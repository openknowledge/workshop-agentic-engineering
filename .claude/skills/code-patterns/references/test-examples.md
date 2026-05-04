# Test Examples

Real test patterns from the Basisprojekt codebase.

## Full Integration Test with Testcontainers

```java
// File: src/test/java/de/openknowledge/baseproject/CounterControllerTest.java
package de.openknowledge.baseproject;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.MediaType.APPLICATION_JSON;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.web.client.RestClient;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;

// WHY: RANDOM_PORT avoids port conflicts between parallel test runs.
// WHY: spring.flyway.enabled=true ensures migrations run and schema exists.
@SpringBootTest(webEnvironment = RANDOM_PORT, properties = "spring.flyway.enabled=true")
class CounterControllerTest {

    // WHY: @ServiceConnection auto-configures Spring datasource to use this container.
    // WHY: static container is shared across all tests in this class.
    @Container @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17");

    @LocalServerPort private int port;

    @Autowired private RestClient.Builder clientBuilder;

    // WHY: @BeforeEach ensures each test gets a fresh client with the correct base URL.
    @BeforeEach
    void setupRestClient() {
        clientBuilder.baseUrl("http://localhost:" + port + "/api");
    }

    @Test
    void updateCounter() {
        // Given
        RestClient client = clientBuilder.build();
        var createResponse =
                client.post()
                        .uri("/counters")
                        .contentType(APPLICATION_JSON)
                        .body(
                                """
                                {
                                    "name": "test",
                                    "value": 0
                                }
                                """)
                        .retrieve()
                        .toBodilessEntity();
        assertTrue(createResponse.getStatusCode().is2xxSuccessful(), "Creation successfull");

        // When
        var response = client.put().uri("/counters/test").body(42).retrieve().toBodilessEntity();
        assertTrue(response.getStatusCode().is2xxSuccessful(), "Update successfull");

        // Then
        int updatedCount =
                client.get()
                        .uri("/counters/test")
                        .accept(APPLICATION_JSON)
                        .retrieve()
                        .body(Integer.class);
        assertThat(updatedCount).isEqualTo(42);
    }
}
```

## Key Testing Patterns

### Testcontainers PostgreSQL Setup
- Always use `postgres:17` image to match production
- `@Container @ServiceConnection` annotation combo handles lifecycle + Spring config
- Static field so container is reused across tests in the class

### RestClient for HTTP Tests
- Inject `RestClient.Builder`, configure base URL in `@BeforeEach`
- Build a new `RestClient` per test for isolation
- Use `.retrieve().toBodilessEntity()` for write operations (POST, PUT)
- Use `.retrieve().body(Type.class)` for read operations (GET)

### Assertion Style
- `assertTrue(response.getStatusCode().is2xxSuccessful(), "message")` for HTTP status
- `assertThat(value).isEqualTo(expected)` for value comparisons (AssertJ)

### Test Data
- Flyway runs `V1.0.0__create-counter.sql` which seeds initial data
- Additional test data in `src/test/resources/db/testdata.sql` (loaded via Flyway testdata location in docker-compose)
- For test-specific data, create it via POST in the Given phase

### Test Class Convention
- Package: `de.openknowledge.baseproject` (same as application root)
- Class name: `<Feature>Test` (not `<Feature>Tests`)
- No `@Testcontainers` annotation needed when using `@ServiceConnection`
- License header required
