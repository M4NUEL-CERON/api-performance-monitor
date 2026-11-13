package com.example.apiperformancemonitor.service;

import com.example.apiperformancemonitor.component.RpsTracker;
import com.example.apiperformancemonitor.model.EndpointMetrics;
import com.example.apiperformancemonitor.model.MetricsSummary;
import com.example.apiperformancemonitor.model.SystemMetrics;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.distribution.HistogramSnapshot;
import io.micrometer.core.instrument.distribution.ValueAtPercentile;
import org.springframework.stereotype.Service;

import java.lang.management.ManagementFactory;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class MetricsService {

    private final MeterRegistry registry;
    private final RpsTracker rpsTracker;

    public MetricsService(MeterRegistry registry, RpsTracker rpsTracker) {
        this.registry = registry;
        this.rpsTracker = rpsTracker;
    }

    public MetricsSummary getSummary() {
        MetricsSummary summary = new MetricsSummary();
        summary.setTimestamp(LocalDateTime.now());
        summary.setRps(buildRpsMetrics());
        summary.setSystem(buildSystemMetrics());
        summary.setEndpoints(buildEndpointMetrics());
        return summary;
    }

    private MetricsSummary.RpsMetrics buildRpsMetrics() {
        MetricsSummary.RpsMetrics rps = new MetricsSummary.RpsMetrics();
        rps.setCurrentRps(rpsTracker.getCurrentRps());
        rps.setAvgRps60s(round(rpsTracker.getAvgRps60s(), 2));
        return rps;
    }

    private SystemMetrics buildSystemMetrics() {
        SystemMetrics s = new SystemMetrics();

        double sysCpu = gaugeValue("system.cpu.usage");
        double procCpu = gaugeValue("process.cpu.usage");
        s.setSystemCpuPercent(sysCpu >= 0 ? round(sysCpu * 100, 2) : -1);
        s.setProcessCpuPercent(procCpu >= 0 ? round(procCpu * 100, 2) : -1);

        // JVM memory: sumamos todos los pools de la misma area
        double heapUsed = gaugeSum("jvm.memory.used", "area", "heap") / (1024.0 * 1024);
        double heapMax  = gaugeSum("jvm.memory.max",  "area", "heap") / (1024.0 * 1024);
        double nonHeap  = gaugeSum("jvm.memory.used", "area", "nonheap") / (1024.0 * 1024);

        s.setHeapUsedMb(round(heapUsed, 2));
        s.setHeapMaxMb(round(heapMax, 2));
        s.setHeapUsedPercent(heapMax > 0 ? round((heapUsed / heapMax) * 100, 2) : 0);
        s.setNonHeapUsedMb(round(nonHeap, 2));
        s.setAvailableProcessors(Runtime.getRuntime().availableProcessors());
        s.setUptimeSeconds(ManagementFactory.getRuntimeMXBean().getUptime() / 1000);
        return s;
    }

    private List<EndpointMetrics> buildEndpointMetrics() {
        // clave = "METHOD /uri"
        Map<String, EndpointMetrics> grouped = new LinkedHashMap<>();
        // para cada clave, acumula status → count
        Map<String, Map<String, Long>> statusMap = new LinkedHashMap<>();
        // guardamos el timer con más requests para usar sus percentiles
        Map<String, Timer> dominantTimer = new LinkedHashMap<>();

        registry.getMeters().stream()
            .filter(m -> "http.server.requests".equals(m.getId().getName()))
            .filter(m -> m instanceof Timer)
            .forEach(meter -> {
                Timer timer = (Timer) meter;
                String method = meter.getId().getTag("method");
                String uri    = meter.getId().getTag("uri");
                String status = meter.getId().getTag("status");

                if (method == null || uri == null) return;
                if (uri.startsWith("/actuator")) return;
                // Unmatched routes (bots probing /wp-login.php, /.env, ...) all collapse into these tags.
                if (uri.equals("/**") || uri.equals("UNKNOWN")) return;

                String key = method + " " + uri;

                EndpointMetrics em = grouped.computeIfAbsent(key, k -> {
                    EndpointMetrics e = new EndpointMetrics();
                    e.setMethod(method);
                    e.setUri(uri);
                    return e;
                });

                em.setTotalRequests(em.getTotalRequests() + timer.count());

                // El timer dominante es el que tiene más requests (suele ser el 2xx)
                Timer current = dominantTimer.get(key);
                if (current == null || timer.count() > current.count()) {
                    dominantTimer.put(key, timer);
                }

                // Acumula status codes
                if (status != null && timer.count() > 0) {
                    statusMap.computeIfAbsent(key, k -> new LinkedHashMap<>())
                        .merge(status, timer.count(), Long::sum);
                }
            });

        // Aplica estadísticas de latencia desde el timer dominante
        grouped.forEach((key, em) -> {
            Timer t = dominantTimer.get(key);
            if (t != null && t.count() > 0) {
                em.setMeanLatencyMs(round(t.mean(TimeUnit.MILLISECONDS), 3));
                em.setMaxLatencyMs(round(t.max(TimeUnit.MILLISECONDS), 3));

                HistogramSnapshot snap = t.takeSnapshot();
                for (ValueAtPercentile vap : snap.percentileValues()) {
                    double ms = round(vap.value(TimeUnit.MILLISECONDS), 3);
                    if (vap.percentile() == 0.5)   em.setP50Ms(ms);
                    else if (vap.percentile() == 0.99)  em.setP99Ms(ms);
                    else if (vap.percentile() == 0.995) em.setP995Ms(ms);
                }
            }
            em.setStatusCodes(statusMap.getOrDefault(key, Map.of()));
        });

        List<EndpointMetrics> result = new ArrayList<>(grouped.values());
        result.sort(Comparator.comparingLong(EndpointMetrics::getTotalRequests).reversed());
        return result;
    }

    // Lee el valor de un Gauge simple (ej: cpu)
    private double gaugeValue(String name) {
        Gauge g = registry.find(name).gauge();
        return g != null ? g.value() : 0.0;
    }

    // Suma todos los Gauges que comparten un tag (ej: todos los pools de heap)
    private double gaugeSum(String name, String tagKey, String tagValue) {
        return registry.find(name).tag(tagKey, tagValue).gauges()
            .stream().mapToDouble(Gauge::value).sum();
    }

    private double round(double value, int decimals) {
        double scale = Math.pow(10, decimals);
        return Math.round(value * scale) / scale;
    }
}