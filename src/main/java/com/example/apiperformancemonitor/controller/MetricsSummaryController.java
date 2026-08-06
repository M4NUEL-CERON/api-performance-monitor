package com.example.apiperformancemonitor.controller;

import com.example.apiperformancemonitor.model.MetricsSummary;
import com.example.apiperformancemonitor.service.MetricsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/metrics")
public class MetricsSummaryController {

    private final MetricsService metricsService;

    public MetricsSummaryController(MetricsService metricsService) {
        this.metricsService = metricsService;
    }

    @GetMapping("/summary")
    public ResponseEntity<MetricsSummary> summary() {
        return ResponseEntity.ok(metricsService.getSummary());
    }
}