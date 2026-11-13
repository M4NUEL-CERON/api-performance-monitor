package com.example.apiperformancemonitor.service;

import com.example.apiperformancemonitor.component.RpsTracker;
import com.example.apiperformancemonitor.model.EndpointMetrics;
import com.example.apiperformancemonitor.model.MetricsSummary;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class MetricsServiceTest {

    private final SimpleMeterRegistry registry = new SimpleMeterRegistry();
    private final RpsTracker rpsTracker = new RpsTracker(registry);
    private final MetricsService service = new MetricsService(registry, rpsTracker);

    private void record(String method, String uri, String status, int n, long ms) {
        Timer timer = Timer.builder("http.server.requests")
            .tags("method", method, "uri", uri, "status", status)
            .publishPercentiles(0.5, 0.99, 0.995)
            .register(registry);
        for (int i = 0; i < n; i++) timer.record(Duration.ofMillis(ms));
    }

    @Test
    void groupsTimersByMethodAndUriAndMergesStatusCodes() {
        record("GET", "/api/products/{id}", "200", 10, 10);
        record("GET", "/api/products/{id}", "404", 2, 1);
        record("GET", "/api/transactions/report", "200", 3, 500);

        MetricsSummary summary = service.getSummary();

        assertThat(summary.getEndpoints()).extracting(EndpointMetrics::getUri)
            .containsExactly("/api/products/{id}", "/api/transactions/report"); // sorted by requests, desc

        EndpointMetrics products = summary.getEndpoints().getFirst();
        assertThat(products.getMethod()).isEqualTo("GET");
        assertThat(products.getTotalRequests()).isEqualTo(12);
        assertThat(products.getStatusCodes()).isEqualTo(Map.of("200", 10L, "404", 2L));
    }

    @Test
    void latencyComesFromTheTimerWithTheMostRequests() {
        record("GET", "/api/products/{id}", "200", 10, 10);
        record("GET", "/api/products/{id}", "404", 2, 900);

        EndpointMetrics e = service.getSummary().getEndpoints().getFirst();

        // Histogram percentiles are approximations, so assert a band rather than an exact value.
        assertThat(e.getP50Ms()).isBetween(9.0, 12.0);
        assertThat(e.getP99Ms()).isBetween(9.0, 12.0);
        assertThat(e.getMeanLatencyMs()).isEqualTo(10.0);
        assertThat(e.getMaxLatencyMs()).isEqualTo(10.0);
    }

    @Test
    void excludesActuatorEndpoints() {
        record("GET", "/actuator/prometheus", "200", 50, 1);
        record("GET", "/api/products", "200", 1, 1);

        assertThat(service.getSummary().getEndpoints()).extracting(EndpointMetrics::getUri)
            .containsExactly("/api/products");
    }

    @Test
    void reportsRpsFromTheTracker() {
        record("GET", "/api/products", "200", 4, 1);
        rpsTracker.tick();

        MetricsSummary.RpsMetrics rps = service.getSummary().getRps();
        assertThat(rps.getCurrentRps()).isEqualTo(4);
        assertThat(rps.getAvgRps60s()).isEqualTo(4);
    }

    @Test
    void systemMetricsDoNotFailWithoutJvmGauges() {
        MetricsSummary summary = service.getSummary();

        assertThat(summary.getEndpoints()).isEmpty();
        assertThat(summary.getSystem().getAvailableProcessors()).isPositive();
        assertThat(summary.getSystem().getHeapUsedPercent()).isZero();
    }
}
