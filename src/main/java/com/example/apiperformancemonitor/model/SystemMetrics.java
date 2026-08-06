package com.example.apiperformancemonitor.model;

public class SystemMetrics {
    private double systemCpuPercent;
    private double processCpuPercent;
    private double heapUsedMb;
    private double heapMaxMb;
    private double heapUsedPercent;
    private double nonHeapUsedMb;
    private int availableProcessors;
    private long uptimeSeconds;

    public double getSystemCpuPercent() { return systemCpuPercent; }
    public void setSystemCpuPercent(double v) { this.systemCpuPercent = v; }
    public double getProcessCpuPercent() { return processCpuPercent; }
    public void setProcessCpuPercent(double v) { this.processCpuPercent = v; }
    public double getHeapUsedMb() { return heapUsedMb; }
    public void setHeapUsedMb(double v) { this.heapUsedMb = v; }
    public double getHeapMaxMb() { return heapMaxMb; }
    public void setHeapMaxMb(double v) { this.heapMaxMb = v; }
    public double getHeapUsedPercent() { return heapUsedPercent; }
    public void setHeapUsedPercent(double v) { this.heapUsedPercent = v; }
    public double getNonHeapUsedMb() { return nonHeapUsedMb; }
    public void setNonHeapUsedMb(double v) { this.nonHeapUsedMb = v; }
    public int getAvailableProcessors() { return availableProcessors; }
    public void setAvailableProcessors(int v) { this.availableProcessors = v; }
    public long getUptimeSeconds() { return uptimeSeconds; }
    public void setUptimeSeconds(long v) { this.uptimeSeconds = v; }
}