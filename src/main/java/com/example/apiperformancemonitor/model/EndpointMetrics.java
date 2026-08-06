package com.example.apiperformancemonitor.model;

import java.util.Map;

public class EndpointMetrics {
    private String method;
    private String uri;
    private long totalRequests;
    private double meanLatencyMs;
    private double maxLatencyMs;
    private double p50Ms;
    private double p99Ms;
    private double p995Ms;
    private Map<String, Long> statusCodes;

    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }
    public String getUri() { return uri; }
    public void setUri(String uri) { this.uri = uri; }
    public long getTotalRequests() { return totalRequests; }
    public void setTotalRequests(long totalRequests) { this.totalRequests = totalRequests; }
    public double getMeanLatencyMs() { return meanLatencyMs; }
    public void setMeanLatencyMs(double meanLatencyMs) { this.meanLatencyMs = meanLatencyMs; }
    public double getMaxLatencyMs() { return maxLatencyMs; }
    public void setMaxLatencyMs(double maxLatencyMs) { this.maxLatencyMs = maxLatencyMs; }
    public double getP50Ms() { return p50Ms; }
    public void setP50Ms(double p50Ms) { this.p50Ms = p50Ms; }
    public double getP99Ms() { return p99Ms; }
    public void setP99Ms(double p99Ms) { this.p99Ms = p99Ms; }
    public double getP995Ms() { return p995Ms; }
    public void setP995Ms(double p995Ms) { this.p995Ms = p995Ms; }
    public Map<String, Long> getStatusCodes() { return statusCodes; }
    public void setStatusCodes(Map<String, Long> statusCodes) { this.statusCodes = statusCodes; }
}