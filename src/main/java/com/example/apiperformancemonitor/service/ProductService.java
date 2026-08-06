package com.example.apiperformancemonitor.service;

import com.example.apiperformancemonitor.model.Product;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class ProductService {

    private final Map<Long, Product> store = new ConcurrentHashMap<>();
    private final AtomicLong idCounter = new AtomicLong(1);
    private final Random random = new Random();

    @PostConstruct
    public void seed() {
        Object[][] data = {
            {"Laptop Pro", "Electronics", "1299.99", 50},
            {"Wireless Mouse", "Electronics", "29.99", 200},
            {"Mechanical Keyboard", "Electronics", "89.99", 150},
            {"4K Monitor", "Electronics", "549.99", 75},
            {"USB-C Hub", "Electronics", "49.99", 300},
            {"Standing Desk", "Furniture", "599.99", 30},
            {"Ergonomic Chair", "Furniture", "399.99", 45},
            {"Webcam HD", "Electronics", "79.99", 120},
            {"Noise Cancelling Headphones", "Electronics", "249.99", 80},
            {"LED Desk Lamp", "Furniture", "39.99", 250}
        };
        for (Object[] row : data) {
            long id = idCounter.getAndIncrement();
            store.put(id, new Product(id, (String) row[0], (String) row[1],
                new BigDecimal((String) row[2]), (Integer) row[3], LocalDateTime.now()));
        }
    }

    // Simula un scan de tabla: 50–150 ms normalmente
    public List<Product> findAll() {
        simulateLatency(50, 150);
        return new ArrayList<>(store.values());
    }

    // Simula un lookup por índice: 5–20 ms
    public Optional<Product> findById(Long id) {
        simulateLatency(5, 20);
        return Optional.ofNullable(store.get(id));
    }

    // Simula un INSERT: 10–30 ms
    public Product create(Product product) {
        simulateLatency(10, 30);
        long id = idCounter.getAndIncrement();
        product.setId(id);
        product.setCreatedAt(LocalDateTime.now());
        store.put(id, product);
        return product;
    }

    // Simula un UPDATE: 10–30 ms
    public Optional<Product> update(Long id, Product updated) {
        simulateLatency(10, 30);
        return Optional.ofNullable(store.computeIfPresent(id, (k, existing) -> {
            existing.setName(updated.getName());
            existing.setCategory(updated.getCategory());
            existing.setPrice(updated.getPrice());
            existing.setStock(updated.getStock());
            return existing;
        }));
    }

    // Simula un DELETE: 5–15 ms
    public boolean delete(Long id) {
        simulateLatency(5, 15);
        return store.remove(id) != null;
    }

    // Endpoint "pesado": 10% de los requests tardan 500–1500 ms → genera cola en p99/p99.5
    public Map<String, Object> getAnalytics() {
        if (random.nextInt(10) == 0) {
            simulateLatency(500, 1500);
        } else {
            simulateLatency(100, 300);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        Map<String, Long> byCategory = new LinkedHashMap<>();
        Map<String, Double> sumByCategory = new LinkedHashMap<>();

        store.values().forEach(p -> {
            byCategory.merge(p.getCategory(), 1L, Long::sum);
            sumByCategory.merge(p.getCategory(), p.getPrice().doubleValue(), Double::sum);
        });

        Map<String, Double> avgPriceByCategory = new LinkedHashMap<>();
        sumByCategory.forEach((cat, sum) ->
            avgPriceByCategory.put(cat, Math.round((sum / byCategory.get(cat)) * 100.0) / 100.0));

        double totalValue = store.values().stream()
            .mapToDouble(p -> p.getPrice().doubleValue() * p.getStock())
            .sum();

        result.put("totalProducts", store.size());
        result.put("totalByCategory", byCategory);
        result.put("avgPriceByCategory", avgPriceByCategory);
        result.put("totalInventoryValue", Math.round(totalValue * 100.0) / 100.0);
        return result;
    }

    private void simulateLatency(int minMs, int maxMs) {
        try {
            Thread.sleep(minMs + random.nextInt(maxMs - minMs + 1));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}