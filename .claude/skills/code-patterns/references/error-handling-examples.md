# Error Handling Examples

Real patterns from the Basisprojekt codebase.

## Controller Layer: ResponseStatusException

Use `ResponseStatusException` to return HTTP errors with appropriate status codes.

```java
// From CounterController.java
import static org.springframework.http.HttpStatus.NOT_FOUND;

@GetMapping(path = "/{name}", produces = APPLICATION_JSON_VALUE)
public CounterValue getCounter(@PathVariable("name") CounterName name) {
    return counterService
            .findCounter(name)
            .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Counter not found"));
}
```

**Pattern:** Service returns `Optional<T>`, controller calls `.orElseThrow()` with `ResponseStatusException`.

## Service Layer: Optional and ifPresentOrElse

```java
// From CounterService.java
public Optional<CounterValue> findCounter(CounterName name) {
    // WHY: Optional propagated to caller -- let the controller decide the HTTP response
    return repository.find(name).map(CounterEntity::getCounter).map(Counter::value);
}

public void updateCounter(CounterName name, CounterValue value) {
    // WHY: ifPresentOrElse handles both found and not-found cases
    repository
            .find(name)
            .ifPresentOrElse(
                    counter -> counter.update(new Counter(name, value)),
                    EntityNotFoundException::new);
}
```

## Entity Layer: requireNonNull

```java
// From CounterEntity.java
import static java.util.Objects.requireNonNull;

public CounterEntity(Counter counter) {
    // WHY: Fail fast on null -- prevents NullPointerException later
    this.counter = requireNonNull(counter);
}

public void update(Counter newCounter) {
    counter = requireNonNull(newCounter);
}
```

## Frontend: try-catch with console.error

```typescript
// From App.tsx
const fetchCount = async () => {
    try {
        const response = await fetch('api/counters/example');
        const value = await response.json();
        setCount(Number(value));
    } catch (e) {
        console.error(e);
    }
};
```

## What NOT to do

- Do NOT use `System.out.println()` or `System.err.println()` -- checkstyle will fail the build
- Do NOT use `.printStackTrace()` -- checkstyle will fail the build
- Do NOT throw generic `Exception` or `RuntimeException` -- use specific exceptions
- Do NOT use `getSingleResult()` for queries that may return no result -- use `getResultStream().findFirst()` to get `Optional`
