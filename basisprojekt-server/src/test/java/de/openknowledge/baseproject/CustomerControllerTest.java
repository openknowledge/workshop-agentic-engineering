/*
 * Copyright (C) open knowledge GmbH.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.openknowledge.baseproject;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.MediaType.APPLICATION_JSON;

import de.openknowledge.baseproject.infrastructure.test.IntegrationTest;
import java.net.URI;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.client.RestClient;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;

@IntegrationTest
class CustomerControllerTest {

    @Container @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17");

    @LocalServerPort private int port;

    @Autowired private RestClient.Builder clientBuilder;

    @BeforeEach
    void setup() {
        clientBuilder.baseUrl("http://localhost:" + port + "/api");
    }

    @Test
    void createAndGetCustomer() throws Exception {
        // Given
        RestClient client = clientBuilder.build();

        // When
        var createResponse =
                client.post()
                        .uri("/customers")
                        .contentType(APPLICATION_JSON)
                        .body(
                                """
                                {
                                    "firstName": "Max",
                                    "lastName": "Mustermann",
                                    "email": "max.mustermann@example.com",
                                    "phoneNumber": "+49 123 4567890",
                                    "birthDate": "1990-05-15"
                                }
                                """)
                        .retrieve()
                        .toBodilessEntity();

        // Then
        assertThat(createResponse.getStatusCode().value()).isEqualTo(CREATED.value());
        URI location = createResponse.getHeaders().getLocation();
        assertThat(location).isNotNull();

        // When
        Map<String, Object> body =
                client.get()
                        .uri(location)
                        .accept(APPLICATION_JSON)
                        .retrieve()
                        .body(new ParameterizedTypeReference<Map<String, Object>>() {});

        // Then
        assertThat(body.get("firstName")).isEqualTo("Max");
        assertThat(body.get("email")).isEqualTo("max.mustermann@example.com");
        assertThat(body.get("externalUserId")).isEqualTo(11);
    }

    @Test
    void createCustomerWithExistingExternalUserSkipsUserCreation() throws Exception {
        // Given
        RestClient client = clientBuilder.build();

        // When
        var createResponse =
                client.post()
                        .uri("/customers")
                        .contentType(APPLICATION_JSON)
                        .body(
                                """
                                {"firstName":"Known","lastName":"User","email":"known@example.com"}
                                """)
                        .retrieve()
                        .toBodilessEntity();

        // Then
        assertThat(createResponse.getStatusCode().value()).isEqualTo(CREATED.value());
        Map<String, Object> body =
                client.get()
                        .uri(createResponse.getHeaders().getLocation())
                        .accept(APPLICATION_JSON)
                        .retrieve()
                        .body(new ParameterizedTypeReference<Map<String, Object>>() {});
        assertThat(body.get("externalUserId")).isEqualTo(7);
    }

    @Test
    void createCustomerRollsBackWhenLookupFails() throws Exception {
        // Given
        RestClient client = clientBuilder.build();

        // When
        var response =
                client.post()
                        .uri("/customers")
                        .contentType(APPLICATION_JSON)
                        .body(
                                """
                                {"firstName":"Fail","lastName":"Lookup","email":"fail-lookup@example.com"}
                                """)
                        .retrieve()
                        .onStatus(status -> status.value() == 502, (req, res) -> {})
                        .toBodilessEntity();

        // Then
        assertThat(response.getStatusCode().value()).isEqualTo(502);
        String listBody =
                client.get()
                        .uri("/customers")
                        .accept(APPLICATION_JSON)
                        .retrieve()
                        .body(String.class);
        assertThat(listBody).doesNotContain("fail-lookup@example.com");
    }

    @Test
    void createCustomerRollsBackWhenUserCreationFails() throws Exception {
        // Given
        RestClient client = clientBuilder.build();

        // When
        var response =
                client.post()
                        .uri("/customers")
                        .contentType(APPLICATION_JSON)
                        .body(
                                """
                                {"firstName":"Fail","lastName":"CreateUser","email":"fail-create-user@example.com"}
                                """)
                        .retrieve()
                        .onStatus(status -> status.value() == 502, (req, res) -> {})
                        .toBodilessEntity();

        // Then
        assertThat(response.getStatusCode().value()).isEqualTo(502);
        String listBody =
                client.get()
                        .uri("/customers")
                        .accept(APPLICATION_JSON)
                        .retrieve()
                        .body(String.class);
        assertThat(listBody).doesNotContain("fail-create-user@example.com");
    }

    @Test
    void createCustomerRollsBackWhenTodoCreationFails() throws Exception {
        // Given
        RestClient client = clientBuilder.build();

        // When
        var response =
                client.post()
                        .uri("/customers")
                        .contentType(APPLICATION_JSON)
                        .body(
                                """
                                {"firstName":"Fail","lastName":"Todo","email":"fail-todo@example.com"}
                                """)
                        .retrieve()
                        .onStatus(status -> status.value() == 502, (req, res) -> {})
                        .toBodilessEntity();

        // Then
        assertThat(response.getStatusCode().value()).isEqualTo(502);
        String listBody =
                client.get()
                        .uri("/customers")
                        .accept(APPLICATION_JSON)
                        .retrieve()
                        .body(String.class);
        assertThat(listBody).doesNotContain("fail-todo@example.com");
    }

    @Test
    void listCustomers() throws Exception {
        // Given
        RestClient client = clientBuilder.build();
        var createResponse =
                client.post()
                        .uri("/customers")
                        .contentType(APPLICATION_JSON)
                        .body(
                                """
                                {
                                    "firstName": "Erika",
                                    "lastName": "Musterfrau",
                                    "email": "erika.musterfrau@example.com"
                                }
                                """)
                        .retrieve()
                        .toBodilessEntity();
        assertTrue(createResponse.getStatusCode().is2xxSuccessful(), "Creation successful");

        // When
        String listBody =
                client.get()
                        .uri("/customers")
                        .accept(APPLICATION_JSON)
                        .retrieve()
                        .body(String.class);

        // Then
        assertThat(listBody).contains("erika.musterfrau@example.com");
    }

    @Test
    void updateCustomer() throws Exception {
        // Given
        RestClient client = clientBuilder.build();
        var createResponse =
                client.post()
                        .uri("/customers")
                        .contentType(APPLICATION_JSON)
                        .body(
                                """
                                {"firstName":"Before","lastName":"Update","email":"update-test@example.com"}
                                """)
                        .retrieve()
                        .toBodilessEntity();

        // When
        var updateResponse =
                client.put()
                        .uri(createResponse.getHeaders().getLocation())
                        .contentType(APPLICATION_JSON)
                        .body(
                                """
                                {"firstName":"After","lastName":"Update","email":"update-test@example.com"}
                                """)
                        .retrieve()
                        .toBodilessEntity();
        assertTrue(updateResponse.getStatusCode().is2xxSuccessful(), "Update successful");

        // Then
        Map<String, Object> updated =
                client.get()
                        .uri(createResponse.getHeaders().getLocation())
                        .accept(APPLICATION_JSON)
                        .retrieve()
                        .body(new ParameterizedTypeReference<Map<String, Object>>() {});
        assertThat(updated.get("firstName")).isEqualTo("After");
        assertThat(updated.get("externalUserId")).isEqualTo(11);
    }

    @Test
    void deleteCustomer() throws Exception {
        // Given
        RestClient client = clientBuilder.build();
        var createResponse =
                client.post()
                        .uri("/customers")
                        .contentType(APPLICATION_JSON)
                        .body(
                                """
                                {
                                    "firstName": "ToDelete",
                                    "lastName": "User",
                                    "email": "delete-test@example.com"
                                }
                                """)
                        .retrieve()
                        .toBodilessEntity();
        assertThat(createResponse.getStatusCode().value()).isEqualTo(201);
        URI location = createResponse.getHeaders().getLocation();
        assertThat(location).isNotNull();

        // When
        var deleteResponse = client.delete().uri(location).retrieve().toBodilessEntity();
        assertTrue(deleteResponse.getStatusCode().is2xxSuccessful(), "Delete successful");

        // Then -- GET should return 404
        var notFoundResponse =
                client.get()
                        .uri(location)
                        .accept(APPLICATION_JSON)
                        .retrieve()
                        .onStatus(status -> status.value() == 404, (req, res) -> {})
                        .toBodilessEntity();
        assertThat(notFoundResponse.getStatusCode().value()).isEqualTo(404);
    }
}
