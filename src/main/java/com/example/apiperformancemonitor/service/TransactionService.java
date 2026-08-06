package com.example.apiperformancemonitor.service;

import com.example.apiperformancemonitor.model.Transaction;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class TransactionService {

    private final Map<Long, Transaction> store = new ConcurrentHashMap<>();
    private final AtomicLong idCounter = new AtomicLong(1);
    private final Random random = new Random();

    private static final String[] TYPES = {"CREDIT", "DEBIT"};
    private static final String[] CURRENCIES = {"USD", "EUR", "COP", "MXN"};
    private static final String[] STATUSES = {"COMPLETED", "COMPLETED", "COMPLETED", "PENDING", "FAILED"};
    private static final String[] DESCRIPTIONS = {
        "Salary payment", "Rent", "Groceries", "Transfer",
        "Subscription", "Insurance", "Utilities", "Bonus"
    };

    @PostConstruct
    public void seed() {
        for (int i = 0; i < 20; i++) {
            long id = idCounter.getAndIncrement();
            store.put(id, new Transaction(
                id,
                TYPES[random.nextInt(TYPES.length)],
                BigDecimal.valueOf(10 + random.nextDouble() * 9990).setScale(2, RoundingMode.HALF_UP),
                CURRENCIES[random.nextInt(CURRENCIES.length)],
                STATUSES[random.nextInt(STATUSES.length)],
                DESCRIPTIONS[random.nextInt(DESCRIPTIONS.length)],
                LocalDateTime.now().minusDays(random.nextInt(30))
            ));
        }
    }

    // Simula lectura de tabla de transacciones: 30–100 ms
    public List<Transaction> findAll() {
        simulateLatency(30, 100);
        return new ArrayList<>(store.values());
    }

    // Simula lookup por ID: 5–15 ms
    public Optional<Transaction> findById(Long id) {
        simulateLatency(5, 15);
        return Optional.ofNullable(store.get(id));
    }

    // Simula crear transacción con validaciones: 20–50 ms
    public Transaction create(Transaction transaction) {
        simulateLatency(20, 50);
        long id = idCounter.getAndIncrement();
        transaction.setId(id);
        transaction.setTimestamp(LocalDateTime.now());
        if (transaction.getStatus() == null) transaction.setStatus("PENDING");
        store.put(id, transaction);
        return transaction;
    }

    // Reporte financiero: muy pesado. 5% de requests son extremadamente lentos → muestra la cola p99.5
    public Map<String, Object> getReport() {
        if (random.nextInt(20) == 0) {
            simulateLatency(1000, 2000); // 5% → cola extrema
        } else if (random.nextInt(10) == 0) {
            simulateLatency(400, 800);   // 10% → cola moderada
        } else {
            simulateLatency(200, 500);   // 85% → camino normal
        }

        long completed = store.values().stream().filter(t -> "COMPLETED".equals(t.getStatus())).count();
        long pending   = store.values().stream().filter(t -> "PENDING".equals(t.getStatus())).count();
        long failed    = store.values().stream().filter(t -> "FAILED".equals(t.getStatus())).count();
        long total     = store.size();

        double totalVolume = store.values().stream()
            .filter(t -> "COMPLETED".equals(t.getStatus()))
            .mapToDouble(t -> t.getAmount().doubleValue())
            .sum();

        Map<String, Long> byCurrency = new LinkedHashMap<>();
        Map<String, Long> byType = new LinkedHashMap<>();
        store.values().forEach(t -> {
            byCurrency.merge(t.getCurrency(), 1L, Long::sum);
            byType.merge(t.getType(), 1L, Long::sum);
        });

        Map<String, Object> report = new LinkedHashMap<>();
        report.put("totalTransactions", total);
        report.put("completed", completed);
        report.put("pending", pending);
        report.put("failed", failed);
        report.put("successRate", total > 0 ? Math.round((completed * 100.0 / total) * 100.0) / 100.0 : 0);
        report.put("totalVolume", Math.round(totalVolume * 100.0) / 100.0);
        report.put("byCurrency", byCurrency);
        report.put("byType", byType);
        return report;
    }

    private void simulateLatency(int minMs, int maxMs) {
        try {
            Thread.sleep(minMs + random.nextInt(maxMs - minMs + 1));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}