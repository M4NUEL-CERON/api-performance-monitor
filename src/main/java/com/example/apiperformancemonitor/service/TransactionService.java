package com.example.apiperformancemonitor.service;

import com.example.apiperformancemonitor.model.Transaction;
import com.example.apiperformancemonitor.repository.TransactionRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class TransactionService {

    private final TransactionRepository repository;
    private final Random random = new Random();

    public TransactionService(TransactionRepository repository) {
        this.repository = repository;
    }

    public List<Transaction> findAll() {
        return repository.findAll();
    }

    public Optional<Transaction> findById(Long id) {
        return repository.findById(id);
    }

    public Transaction create(Transaction transaction) {
        transaction.setId(null);
        transaction.setTimestamp(LocalDateTime.now());
        if (transaction.getStatus() == null) transaction.setStatus("PENDING");
        return repository.save(transaction);
    }

    // Reporte financiero pesado: 5% de requests tardan 1-2s → p99.5 muy alto
    public Map<String, Object> getReport() {
        if (random.nextInt(20) == 0) {
            simulateLatency(800, 1800);
        } else if (random.nextInt(10) == 0) {
            simulateLatency(300, 600);
        }

        long total      = repository.count();
        long completed  = repository.countByStatus("COMPLETED");
        long pending    = repository.countByStatus("PENDING");
        long failed     = repository.countByStatus("FAILED");
        Double volume   = repository.getTotalCompletedVolume();

        Map<String, Long> byCurrency = toMap(repository.countGroupedByCurrency());
        Map<String, Long> byType     = toMap(repository.countGroupedByType());

        Map<String, Object> report = new LinkedHashMap<>();
        report.put("totalTransactions", total);
        report.put("completed", completed);
        report.put("pending", pending);
        report.put("failed", failed);
        report.put("successRate", total > 0 ? Math.round((completed * 100.0 / total) * 100.0) / 100.0 : 0);
        report.put("totalVolume", volume != null ? Math.round(volume * 100.0) / 100.0 : 0);
        report.put("byCurrency", byCurrency);
        report.put("byType", byType);
        return report;
    }

    private Map<String, Long> toMap(List<Object[]> rows) {
        Map<String, Long> map = new LinkedHashMap<>();
        for (Object[] row : rows) {
            map.put((String) row[0], (Long) row[1]);
        }
        return map;
    }

    private void simulateLatency(int minMs, int maxMs) {
        try {
            Thread.sleep(minMs + random.nextInt(maxMs - minMs + 1));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
