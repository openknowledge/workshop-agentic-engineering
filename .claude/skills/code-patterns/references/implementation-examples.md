# Implementation Examples

Real examples extracted from the Basisprojekt codebase showing how to implement each layer.

## 1. Value Object Record

Value objects are `@Embeddable` records with `@JsonValue` for JSON serialization.

```java
// File: src/main/java/de/openknowledge/baseproject/<feature>/domain/<Name>.java
package de.openknowledge.baseproject.counter.domain;

import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.persistence.Embeddable;

// WHY: Records are immutable value objects. @Embeddable lets JPA embed them in entities.
// WHY: @JsonValue on the component makes Jackson serialize/deserialize the raw value directly.
@Embeddable
public record CounterName(@JsonValue String name) {}
```

```java
@Embeddable
public record CounterValue(@JsonValue int value) {}
```

## 2. Aggregate Record

Composes multiple value objects into a domain aggregate.

```java
// File: src/main/java/de/openknowledge/baseproject/counter/domain/Counter.java
package de.openknowledge.baseproject.counter.domain;

import jakarta.persistence.Embeddable;

// WHY: The aggregate is also @Embeddable so it can be embedded in the entity.
// This separates domain logic from persistence concerns.
@Embeddable
public record Counter(CounterName name, CounterValue value) {}
```

## 3. Entity Class

Wraps the domain aggregate with JPA persistence annotations.

```java
// File: src/main/java/de/openknowledge/baseproject/counter/domain/CounterEntity.java
package de.openknowledge.baseproject.counter.domain;

import static jakarta.persistence.GenerationType.SEQUENCE;
import static java.util.Objects.requireNonNull;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

// WHY: Entity name is simple ("Counter") for JPQL, class name has Entity suffix for Java clarity.
// WHY: @NamedQuery defined here keeps queries close to the entity they operate on.
// WHY: Query name constant is public static final so the repository can reference it.
@Entity(name = "Counter")
@Table(name = "TAB_COUNTER")
@NamedQuery(
        name = CounterEntity.BY_NAME,
        query = "SELECT c FROM Counter c WHERE c.counter.name.name = :name")
public class CounterEntity {

    public static final String BY_NAME = "CounterEntity.byName";

    @Id
    @Column(name = "C_ID")
    @GeneratedValue(strategy = SEQUENCE, generator = "SEQ_COUNTER")
    @SequenceGenerator(name = "SEQ_COUNTER", sequenceName = "SEQ_C_ID", allocationSize = 1)
    private Long id;

    // WHY: @AttributeOverride maps the nested record fields to specific DB columns.
    @Embedded
    @AttributeOverride(name = "name.name", column = @Column(name = "C_NAME"))
    @AttributeOverride(name = "value.value", column = @Column(name = "C_VALUE"))
    private Counter counter;

    // WHY: requireNonNull enforces non-null invariant at construction time.
    public CounterEntity(Counter counter) {
        this.counter = requireNonNull(counter);
    }

    // WHY: JPA requires a protected no-arg constructor.
    protected CounterEntity() {
        // for JPA
    }

    public Counter getCounter() {
        return counter;
    }

    public void update(Counter newCounter) {
        counter = requireNonNull(newCounter);
    }
}
```

## 4. Repository

Uses `EntityManager` directly with named queries. NOT Spring Data interfaces.

```java
// File: src/main/java/de/openknowledge/baseproject/counter/domain/CounterRepository.java
package de.openknowledge.baseproject.counter.domain;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.Optional;
import org.springframework.stereotype.Repository;

// WHY: We use EntityManager, not Spring Data JPA interfaces, for explicit control over queries.
@Repository
public class CounterRepository {

    @PersistenceContext private EntityManager entityManager;

    public void persist(CounterEntity counter) {
        entityManager.persist(counter);
    }

    // WHY: Optional return type for queries that may return no result.
    // WHY: getResultStream().findFirst() is safer than getSingleResult() which throws exceptions.
    public Optional<CounterEntity> find(CounterName name) {
        return entityManager
                .createNamedQuery(CounterEntity.BY_NAME, CounterEntity.class)
                .setParameter("name", name.name())
                .getResultStream()
                .findFirst();
    }
}
```

## 5. Service

`@Service @Transactional` with business logic.

```java
// File: src/main/java/de/openknowledge/baseproject/counter/domain/CounterService.java
package de.openknowledge.baseproject.counter.domain;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class CounterService {

    @Autowired private CounterRepository repository;

    public void createCounter(Counter counter) {
        repository.persist(new CounterEntity(counter));
    }

    public Optional<CounterValue> findCounter(CounterName name) {
        return repository.find(name).map(CounterEntity::getCounter).map(Counter::value);
    }

    public void updateCounter(CounterName name, CounterValue value) {
        repository
                .find(name)
                .ifPresentOrElse(
                        counter -> counter.update(new Counter(name, value)),
                        EntityNotFoundException::new);
    }
}
```

## 6. REST Controller

```java
// File: src/main/java/de/openknowledge/baseproject/counter/web/CounterController.java
package de.openknowledge.baseproject.counter.web;

import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import de.openknowledge.baseproject.counter.domain.Counter;
import de.openknowledge.baseproject.counter.domain.CounterName;
import de.openknowledge.baseproject.counter.domain.CounterService;
import de.openknowledge.baseproject.counter.domain.CounterValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

// WHY: Base path /api/<feature> keeps API organized by feature.
@RestController
@RequestMapping(path = "/api/counters")
public class CounterController {

    @Autowired private CounterService counterService;

    @PostMapping(consumes = APPLICATION_JSON_VALUE)
    public void createCounter(@RequestBody Counter counter) {
        counterService.createCounter(counter);
    }

    // WHY: ResponseStatusException with NOT_FOUND for missing entities.
    @GetMapping(path = "/{name}", produces = APPLICATION_JSON_VALUE)
    public CounterValue getCounter(@PathVariable("name") CounterName name) {
        return counterService
                .findCounter(name)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Counter not found"));
    }

    @PutMapping(path = "/{name}", consumes = APPLICATION_JSON_VALUE)
    public void updateCounter(
            @PathVariable("name") CounterName name, @RequestBody CounterValue value) {
        counterService.updateCounter(name, value);
    }
}
```

## 7. Flyway Migration

```sql
-- File: src/main/resources/db/migration/V1.0.0__create-counter.sql
CREATE SEQUENCE seq_c_id
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE TABLE tab_counter (
    c_id BIGINT NOT NULL,
    c_name VARCHAR(255) NOT NULL,
    c_value INTEGER NOT NULL DEFAULT 0,
    CONSTRAINT pk_c_id PRIMARY KEY (c_id),
    CONSTRAINT uq_c_name UNIQUE (c_name)
);

-- Seed data for the example counter
INSERT INTO tab_counter (c_id, c_name, c_value) VALUES (nextval('seq_c_id'), 'example', '0');
```
