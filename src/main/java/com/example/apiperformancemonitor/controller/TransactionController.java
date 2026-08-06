package com.example.apiperformancemonitor.controller;

import com.example.apiperformancemonitor.model.ApiResponse;
import com.example.apiperformancemonitor.model.Transaction;
import com.example.apiperformancemonitor.service.TransactionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Transaction>>> getAll() {
        long t = System.currentTimeMillis();
        List<Transaction> data = transactionService.findAll();
        return ResponseEntity.ok(ApiResponse.success("Transactions retrieved", data, elapsed(t)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Transaction>> getById(@PathVariable Long id) {
        long t = System.currentTimeMillis();
        return transactionService.findById(id)
            .<ResponseEntity<ApiResponse<Transaction>>>map(tx ->
                ResponseEntity.ok(ApiResponse.success("Transaction found", tx, elapsed(t))))
            .orElse(ResponseEntity.status(404).body(ApiResponse.error("Transaction not found", elapsed(t))));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Transaction>> create(@RequestBody Transaction transaction) {
        long t = System.currentTimeMillis();
        Transaction created = transactionService.create(transaction);
        return ResponseEntity.status(201).body(ApiResponse.success("Transaction created", created, elapsed(t)));
    }

    // Reporte financiero: cola extrema en 5% de requests → p99.5 muy alto
    @GetMapping("/report")
    public ResponseEntity<ApiResponse<Map<String, Object>>> report() {
        long t = System.currentTimeMillis();
        Map<String, Object> data = transactionService.getReport();
        return ResponseEntity.ok(ApiResponse.success("Report generated", data, elapsed(t)));
    }

    private long elapsed(long startMs) {
        return System.currentTimeMillis() - startMs;
    }
}