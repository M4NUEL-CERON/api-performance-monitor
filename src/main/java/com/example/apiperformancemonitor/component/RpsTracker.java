package com.example.apiperformancemonitor.component;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Calcula las peticiones por segundo (RPS) a partir de los contadores de Micrometer.
 * Cada segundo compara el total acumulado con el total del segundo anterior.
 */
@Component
public class RpsTracker {

    private final MeterRegistry registry;
    private final Queue<Long> history = new LinkedList<>();
    private final AtomicLong lastTotalCount = new AtomicLong(0);

    private volatile double currentRps = 0.0;
    private volatile double avgRps60s = 0.0;

    public RpsTracker(MeterRegistry registry) {
        this.registry = registry;
    }

    @Scheduled(fixedRate = 1000)
    public void tick() {
        long totalNow = registry.getMeters().stream()
            .filter(m -> "http.server.requests".equals(m.getId().getName()))
            .filter(m -> m instanceof Timer)
            .mapToLong(m -> ((Timer) m).count())
            .sum();

        long delta = totalNow - lastTotalCount.getAndSet(totalNow);
        currentRps = delta;

        history.offer(delta);
        if (history.size() > 60) history.poll();

        avgRps60s = history.stream().mapToLong(Long::longValue).average().orElse(0.0);
    }

    public double getCurrentRps() { return currentRps; }
    public double getAvgRps60s() { return avgRps60s; }
}