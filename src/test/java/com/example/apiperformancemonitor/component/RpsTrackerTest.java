package com.example.apiperformancemonitor.component;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

class RpsTrackerTest {

    private final MeterRegistry registry = new SimpleMeterRegistry();
    private final RpsTracker tracker = new RpsTracker(registry);

    private void requests(String uri, int n) {
        for (int i = 0; i < n; i++) {
            registry.timer("http.server.requests", "uri", uri).record(Duration.ofMillis(1));
        }
    }

    @Test
    void currentRpsIsTheDeltaSinceThePreviousTick() {
        requests("/a", 5);
        tracker.tick();
        assertThat(tracker.getCurrentRps()).isEqualTo(5);

        requests("/a", 2);
        requests("/b", 1);
        tracker.tick();
        assertThat(tracker.getCurrentRps()).isEqualTo(3);

        tracker.tick();
        assertThat(tracker.getCurrentRps()).isZero();
        assertThat(tracker.getAvgRps60s()).isEqualTo((5 + 3 + 0) / 3.0);
    }

    @Test
    void averageOnlyCoversTheLast60Ticks() {
        requests("/a", 120);
        tracker.tick();
        for (int i = 0; i < 59; i++) tracker.tick();
        assertThat(tracker.getAvgRps60s()).isEqualTo(2.0);

        tracker.tick(); // the 120-request tick falls out of the window
        assertThat(tracker.getAvgRps60s()).isZero();
    }

    @Test
    void ignoresOtherMeters() {
        registry.timer("some.other.timer").record(Duration.ofMillis(1));
        registry.counter("http.server.requests.fake").increment();
        tracker.tick();
        assertThat(tracker.getCurrentRps()).isZero();
    }
}
