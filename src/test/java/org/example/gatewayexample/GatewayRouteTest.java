package org.example.gatewayexample;

import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.client.RestClient;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class GatewayRouteTest {

    static MockWebServer mockBackend = new MockWebServer();

    static {
        mockBackend.setDispatcher(new Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest request) {
                return switch (request.getPath()) {
                    case "/api/ok" -> new MockResponse()
                            .setResponseCode(200)
                            .addHeader("Content-Type", "text/plain")
                            .setBody("Success");
                    case "/api/error" -> new MockResponse()
                            .setResponseCode(500)
                            .addHeader("Content-Type", "text/plain");
                    default -> new MockResponse().setResponseCode(404);
                };
            }
        });
    }

    @DynamicPropertySource
    static void gatewayBackendUrl(DynamicPropertyRegistry registry) throws IOException {
        mockBackend.start();
        registry.add("dependencies.some-service.url", () -> mockBackend.url("/").toString()
                .replaceAll("/$", ""));
    }

    @AfterAll
    static void tearDown() throws IOException {
        mockBackend.shutdown();
    }

    @LocalServerPort
    int port;

    RestClient restClient() {
        return RestClient.builder()
                .baseUrl("http://localhost:" + port)
                .defaultStatusHandler(status -> true, (req, resp) -> { })
                .build();
    }

    @Test
    void gatewayReturns200WhenBackendReturnsOk() {
        ResponseEntity<String> response = restClient().get().uri("/api/ok")
                .retrieve()
                .toEntity(String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo("Success");
    }

    @Test
    void gatewayReturns404ForUnmatchedRoute() {
        ResponseEntity<String> response = restClient().get().uri("/no-route/here").header("Content-Type", "application/json")
                .retrieve()
                .toEntity(String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void gatewayReturns500WhenBackendReturnsServerError() {
        ResponseEntity<String> response = restClient().get().uri("/api/error")
                .retrieve()
                .toEntity(String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }
}



