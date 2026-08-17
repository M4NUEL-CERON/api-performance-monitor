package com.example.apiperformancemonitor.service;

import com.example.apiperformancemonitor.model.Product;
import com.example.apiperformancemonitor.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class ProductService {

    private final ProductRepository repository;
    private final Random random = new Random();

    public ProductService(ProductRepository repository) {
        this.repository = repository;
    }

    public List<Product> findAll() {
        return repository.findAll();
    }

    public Optional<Product> findById(Long id) {
        return repository.findById(id);
    }

    public Product create(Product product) {
        product.setId(null);
        product.setCreatedAt(LocalDateTime.now());
        return repository.save(product);
    }

    public Optional<Product> update(Long id, Product updated) {
        return repository.findById(id).map(existing -> {
            existing.setName(updated.getName());
            existing.setCategory(updated.getCategory());
            existing.setPrice(updated.getPrice());
            existing.setStock(updated.getStock());
            return repository.save(existing);
        });
    }

    public boolean delete(Long id) {
        if (!repository.existsById(id)) return false;
        repository.deleteById(id);
        return true;
    }

    // Endpoint pesado: 10% de requests generan latencia extrema → ver p99/p99.5 dispararse
    public Map<String, Object> getAnalytics() {
        if (random.nextInt(10) == 0) {
            simulateLatency(400, 900);
        }

        List<Product> all = repository.findAll();

        Map<String, Long> byCategory = new LinkedHashMap<>();
        Map<String, Double> sumByCategory = new LinkedHashMap<>();
        all.forEach(p -> {
            byCategory.merge(p.getCategory(), 1L, Long::sum);
            sumByCategory.merge(p.getCategory(), p.getPrice().doubleValue(), Double::sum);
        });

        Map<String, Double> avgPriceByCategory = new LinkedHashMap<>();
        sumByCategory.forEach((cat, sum) ->
            avgPriceByCategory.put(cat, Math.round((sum / byCategory.get(cat)) * 100.0) / 100.0));

        double totalValue = all.stream()
            .mapToDouble(p -> p.getPrice().doubleValue() * p.getStock())
            .sum();

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("totalProducts", all.size());
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
