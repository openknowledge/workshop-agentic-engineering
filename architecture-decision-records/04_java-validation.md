# ADR: Validierung im Java-Backend

* Status: proposed
* Deciders:
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
- Um die Constructor-Validierung bei der Deserialisierung von JSON zu ermöglichen, ist eigener Infrastruktur-Code für das Mapping von JSON nach Java Records erforderlich. Dieser Code existiert bereits und wurde von open knowledge mehrfach in Kundenprojekten verwendet. Er müsste allerdings vom Projekt-Team gewartet werden.

#### Vorteile

- Frühzeitige Validierung mit aussagekräftigen Stacktraces
- Aggregierte und strukturierte Fehlermeldungen für REST-Clients
- Stabile Domänenmodelle ohne ungültige Zustände
- Klare Trennung zwischen fachlicher und technischer Validierung

#### Nachteile

- Zusätzliche Komplexität durch eigene Logik in der Deserialisierung
- Höherer initialer Implementierungs- und Wartungsaufwand

## Entscheidungstreiber

Die Entscheidung wurde insbesondere durch folgende Aspekte beeinflusst:
- *Früherkennung von Fehlern:* Ungültige Zustände sollen so früh wie möglich erkannt werden.
- *Nachvollziehbarkeit im Fehlerfall:* Stacktraces müssen eindeutig auf die fachlich relevante Stelle im Code verweisen.
- *Qualität der REST-Fehlermeldungen:* Validierungsfehler sollen gesammelt und strukturiert an Clients zurückgegeben werden können.
- *Stabilität des Domänenmodells:* Das Domänenmodell soll keine ungültigen Zustände repräsentieren können.

## Out of Scope

Nicht Bestandteil dieser Entscheidung sind:
- Validierung auf UI- oder Client-Seite
- Datenbankspezifische Constraints (z. B. NOT NULL, UNIQUE)
- Sicherheits- und Berechtigungsprüfungen
- Validierung asynchroner oder eventbasierter Schnittstellen
