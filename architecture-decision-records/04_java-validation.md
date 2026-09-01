# ADR: Validierung im Java-Backend

* Status: accepted (11.09.2026)
* Deciders: Sören Dierkes, Arne Limburg, Gabriel Winkler
* Date: 21.08.2026

## Kontext und Problembeschreibung

Im Java-Backend werden Daten über REST-Schnittstellen entgegengenommen, in Java-Objekte (insbesondere Records) gemappt und anschließend weiterverarbeitet bzw. persistiert. Dabei ist eine zuverlässige, nachvollziehbare und gut wartbare Validierung erforderlich.

Die Validierung soll sicherstellen, dass:
- fachlich ungültige Zustände frühzeitig erkannt werden,
- aussagekräftige Fehlermeldungen an die REST-Clients zurückgegeben werden können,
- Fehler im Code eindeutig der verursachenden Stelle zugeordnet werden können (z. B. durch Stacktraces),
- das Domänenmodell keine ungültigen Zustände annehmen kann.

Im aktuellen Diskurs wurde deutlich, dass einzelne Validierungsansätze jeweils spezifische Nachteile haben, die sich negativ auf Debugging, Fehlermeldungen oder die Modellkonsistenz auswirken.

## Diskutierte Optionen
### Option 1: Validierung ausschließlich über Bean Validation

Die Validierung erfolgt ausschließlich mittels Bean Validation (z. B. Jakarta Validation) über Annotationen an DTOs oder Records. Die Prüfung wird typischerweise beim Eingang der REST-Anfrage oder implizit bei der Persistierung (z. B. via JPA) durchgeführt.

#### Vorteile

- Standardisierte Lösung mit breiter Tool-Unterstützung
- Automatische Aggregation von Validierungsfehlern für REST-Antworten
- Geringer Implementierungsaufwand

#### Nachteile

- Fehler treten häufig erst spät auf (z.B. beim Speichern in der Datenbank, also beim Commit der Transaktion)
- Stacktraces zeigen nicht auf die fachlich relevante Code-Stelle, sondern auf Infrastruktur- oder Persistenzcode
- Das Domänenmodell kann temporär ungültige Zustände annehmen

### Option 2: Validierung im Code (Constructor und Setter) mit separatem API-Modell

Die fachliche Validierung erfolgt ausschließlich im Code des Domänenmodells, insbesondere in Konstruktoren und Settern. Dadurch wird sichergestellt, dass das Domänenmodell zu keinem Zeitpunkt einen ungültigen Zustand annehmen kann.

Für die REST-Schnittstellen wird zusätzlich ein separates API-Modell (z. B. Request-DTO oder Record) eingeführt, das mit Bean-Validation-Annotationen versehen ist. Dieses API-Modell dient ausschließlich der Eingabevalidierung und der strukturierten Rückmeldung von Validierungsfehlern an den Client. Nach erfolgreicher Validierung wird das API-Modell explizit in das Domänenmodell gemappt.

#### Vorteile

- Das Domänenmodell ist strikt konsistent und kann keine ungültigen Zustände annehmen
- Fehler treten frühzeitig und an der fachlich relevanten Stelle im Code auf
- Stacktraces sind präzise und gut nachvollziehbar
- Bean Validation kann weiterhin zur Sammlung und Strukturierung von Fehlermeldungen für REST-Responses genutzt werden
- Klare Trennung zwischen API-Schicht und Domänenmodell

#### Nachteile

- Zusätzliches API-Modell erhöht die Anzahl der Klassen/Records
- Es ist ein explizites Mapping zwischen API-Modell und Domänenmodell erforderlich
- Redundanz von Validierungslogik möglich (API-Validierung vs. Domänenvalidierung)
- Höherer Implementierungs- und Wartungsaufwand durch Mapping-Code

### Option 3: Kombination aus Bean Validation und Constructor-Validierung

Die Validierung erfolgt zweistufig:
- Bean Validation wird zur deklarativen Validierung an Records eingesetzt, insbesondere zur Unterstützung der REST-Schnittstellen und zur Aggregation von Fehlermeldungen.
- Zusätzlich wird im Konstruktor der Records explizite Validierung durchgeführt, um ungültige Zustände im Domänenmodell zu verhindern und Fehler frühzeitig zu erkennen.
- Um die Constructor-Validierung bei der Deserialisierung von JSON zu ermöglichen, ist eigener Infrastruktur-Code für das Mapping von JSON nach Java Records erforderlich (z. B. ein eigener Record-Instantiator und Deserializer für Jackson, der auch die Fehlerpfade der Constructor-Validierung korrigiert). Dieser Code müsste vom Projekt-Team gewartet werden.

#### Vorteile

- Frühzeitige Validierung mit aussagekräftigen Stacktraces
- Aggregierte und strukturierte Fehlermeldungen für REST-Clients
- Stabile Domänenmodelle ohne ungültige Zustände
- Klare Trennung zwischen fachlicher und technischer Validierung

#### Nachteile

- Zusätzliche Komplexität durch eigene Logik in der Deserialisierung
- Höherer initialer Implementierungs- und Wartungsaufwand
- Das Framework (Jackson, Hibernate, Spring) erzeugt Records über den Konstruktor, sodass Constructor-Validierung und Bean Validation um dieselben Fehler konkurrieren

### Option 4: Bean Validation nach der Konstruktion, Factory-Methoden für Code-erzeugte Objekte

Die Validierungsregeln werden ausschließlich per Bean Validation an den Records definiert und immer nach der Konstruktion ausgewertet:
- Der kanonische Konstruktor enthält keine Validierung. Erlaubt ist nur Normalisierung (z. B. `trim()`). Der Konstruktor muss null-sicher sein, da die Parameter zu diesem Zeitpunkt noch invalide sein können.
- Records, die von Frameworks erzeugt werden (JSON-Body, Request-Parameter, JPA), werden anschließend über `@Valid` am Controller-Parameter (bzw. an Record-Komponenten) validiert. Dafür sind die Controller mit `@Validated` annotiert.
- Records, die im Code erzeugt werden, werden über eine statische Factory-Methode `new<RecordName>` erzeugt, die das Ergebnis sofort über `Validate.validate(...)` validiert und bei Fehlern eine `IllegalArgumentException` wirft.
- Für Path-Variablen besitzt das Value Object einen zusätzlichen Konstruktor mit einem `String`-Parameter, den Spring zur Konvertierung nutzt. Es ist kein eigener `Converter` nötig.
- Die Einhaltung dieser Regeln wird durch ArchUnit-Tests (`ValueObjectTest`) sichergestellt.

#### Vorteile

- Kein eigener Infrastruktur-Code für die Deserialisierung von Records
- Aggregierte und strukturierte Fehlermeldungen für REST-Clients über einen zentralen `ValidationExceptionHandler`
- Die Validierungsregeln stehen an genau einer Stelle (Bean Validation) und werden auch für die OpenAPI-Spezifikation genutzt
- Im Code erzeugte Objekte werden sofort validiert, Stacktraces zeigen auf die erzeugende Stelle
- Kein separates API-Modell nötig

#### Nachteile

- Records, die von Frameworks erzeugt werden, sind zwischen Konstruktion und `@Valid` kurzzeitig potenziell invalide
- Die Konvention "Konstruktor nicht direkt aufrufen" ist nicht durch die Sprache erzwungen und muss durch Architekturtests abgesichert werden
- Der Konstruktor muss mit ungültigen Werten (z. B. `null`) umgehen können

## Entscheidungstreiber

Die Entscheidung wurde insbesondere durch folgende Aspekte beeinflusst:
- *Früherkennung von Fehlern:* Ungültige Zustände sollen so früh wie möglich erkannt werden.
- *Nachvollziehbarkeit im Fehlerfall:* Stacktraces müssen eindeutig auf die fachlich relevante Stelle im Code verweisen.
- *Qualität der REST-Fehlermeldungen:* Validierungsfehler sollen gesammelt und strukturiert an Clients zurückgegeben werden können.
- *Stabilität des Domänenmodells:* Das Domänenmodell soll keine ungültigen Zustände repräsentieren können.

## Entscheidung

Wir entscheiden uns für Option 4.

Die Constructor-Validierung aus Option 3 hat sich nicht bewährt: Sie erfordert eigenen Infrastruktur-Code für Jackson
und steht im Konflikt damit, dass Frameworks Records über den Konstruktor erzeugen und die Validierung dort
ohnehin nachträglich per Bean Validation erfolgt. Die Früherkennung von Fehlern in Code-erzeugten Objekten
erreichen wir stattdessen durch die Factory-Methoden `new<RecordName>`, die sofort validieren.
Die wesentlichen Regeln sind in der Dokumentation `setup_steps/server-side-validation.adoc` beschrieben.

## Out of Scope

Nicht Bestandteil dieser Entscheidung sind:
- Validierung auf UI- oder Client-Seite
- Datenbankspezifische Constraints (z. B. NOT NULL, UNIQUE)
- Sicherheits- und Berechtigungsprüfungen
- Validierung asynchroner oder eventbasierter Schnittstellen
