package com.example.apiperformancemonitor;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.stream.StreamSupport;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class MetricsSummaryIntegrationTest {

    private final HttpClient http = HttpClient.newHttpClient();
    private final JsonMapper json = JsonMapper.builder().build();

    @Value("${local.server.port}")
    private int port;

    private HttpResponse<String> get(String path) throws Exception {
        return get(path, null);
    }

    private HttpResponse<String> get(String path, String origin) throws Exception {
        HttpRequest.Builder req = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path));
        if (origin != null) req.header("Origin", origin);
        return http.send(req.build(), HttpResponse.BodyHandlers.ofString());
    }

    private JsonNode endpoint(JsonNode summary, String uri) {
        return StreamSupport.stream(summary.get("endpoints").spliterator(), false)
            .filter(e -> uri.equals(e.get("uri").asString()))
            .findFirst().orElse(null);
    }

    @Test
    void summaryReflectsRealTraffic() throws Exception {
        assertThat(get("/api/products/1").statusCode()).isEqualTo(200);
        assertThat(get("/api/products/999999").statusCode()).isEqualTo(404);

        // The timer is recorded after the response is written, so allow a short wait.
        JsonNode products = null;
        JsonNode summary = null;
        for (int i = 0; i < 20 && (products == null || products.get("totalRequests").asLong() < 2); i++) {
            HttpResponse<String> res = get("/api/metrics/summary");
            assertThat(res.statusCode()).isEqualTo(200);
            summary = json.readTree(res.body());
            products = endpoint(summary, "/api/products/{id}");
            if (products == null || products.get("totalRequests").asLong() < 2) Thread.sleep(100);
        }

        assertThat(products).isNotNull();
        assertThat(products.get("method").asString()).isEqualTo("GET");
        assertThat(products.get("totalRequests").asLong()).isEqualTo(2);
        assertThat(products.get("statusCodes").get("200").asLong()).isEqualTo(1);
        assertThat(products.get("statusCodes").get("404").asLong()).isEqualTo(1);
        assertThat(products.get("p50Ms").asDouble()).isPositive();

        assertThat(summary.get("rps").has("currentRps")).isTrue();
        assertThat(summary.get("system").get("availableProcessors").asInt()).isPositive();
        assertThat(summary.get("system").get("heapMaxMb").asDouble()).isPositive();
    }

    @Test
    void onlySafeActuatorEndpointsAreExposed() throws Exception {
        assertThat(get("/actuator/health").statusCode()).isEqualTo(200);
        assertThat(get("/actuator/prometheus").statusCode()).isEqualTo(200);
        assertThat(get("/actuator/env").statusCode()).isEqualTo(404);
        assertThat(get("/actuator/heapdump").statusCode()).isEqualTo(404);
        assertThat(get("/h2-console").statusCode()).isEqualTo(404);
    }

    @Test
    void corsAllowsOnlyConfiguredOrigins() throws Exception {
        HttpResponse<String> allowed = get("/api/metrics/summary", "http://localhost:5173");
        assertThat(allowed.headers().firstValue("Access-Control-Allow-Origin")).hasValue("http://localhost:5173");

        assertThat(get("/api/metrics/summary", "https://evil.example").statusCode()).isEqualTo(403);
    }
}
