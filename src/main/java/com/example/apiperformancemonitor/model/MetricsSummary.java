package com.example.apiperformancemonitor.model;

import java.time.LocalDateTime;
import java.util.List;

public class MetricsSummary {
    private LocalDateTime timestamp;
    private RpsMetrics rps;
    private SystemMetrics system;
    private List<EndpointMetrics> endpoints;

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    public RpsMetrics getRps() { return rps; }
    public void setRps(RpsMetrics rps) { this.rps = rps; }
    public SystemMetrics getSystem() { return system; }
    public void setSystem(SystemMetrics system) { this.system = system; }
    public List<EndpointMetrics> getEndpoints() { return endpoints; }
    public void setEndpoints(List<EndpointMetrics> endpoints) { this.endpoints = endpoints; }

    public static class RpsMetrics {
        private double currentRps;
        private double avgRps60s;

        public double getCurrentRps() { return currentRps; }
        public void setCurrentRps(double currentRps) { this.currentRps = currentRps; }
        public double getAvgRps60s() { return avgRps60s; }
        public void setAvgRps60s(double avgRps60s) { this.avgRps60s = avgRps60s; }
    }
}