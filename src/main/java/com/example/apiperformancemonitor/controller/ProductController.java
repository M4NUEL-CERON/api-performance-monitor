package com.example.apiperformancemonitor.controller;

import com.example.apiperformancemonitor.model.ApiResponse;
import com.example.apiperformancemonitor.model.Product;
import com.example.apiperformancemonitor.service.ProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Product>>> getAll() {
        long t = System.currentTimeMillis();
        List<Product> data = productService.findAll();
        return ResponseEntity.ok(ApiResponse.success("Products retrieved", data, elapsed(t)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Product>> getById(@PathVariable Long id) {
        long t = System.currentTimeMillis();
        return productService.findById(id)
            .<ResponseEntity<ApiResponse<Product>>>map(p ->
                ResponseEntity.ok(ApiResponse.success("Product found", p, elapsed(t))))
            .orElse(ResponseEntity.status(404).body(ApiResponse.error("Product not found", elapsed(t))));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Product>> create(@RequestBody Product product) {
        long t = System.currentTimeMillis();
        Product created = productService.create(product);
        return ResponseEntity.status(201).body(ApiResponse.success("Product created", created, elapsed(t)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Product>> update(@PathVariable Long id, @RequestBody Product product) {
        long t = System.currentTimeMillis();
        return productService.update(id, product)
            .<ResponseEntity<ApiResponse<Product>>>map(p ->
                ResponseEntity.ok(ApiResponse.success("Product updated", p, elapsed(t))))
            .orElse(ResponseEntity.status(404).body(ApiResponse.error("Product not found", elapsed(t))));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        long t = System.currentTimeMillis();
        if (productService.delete(id)) {
            return ResponseEntity.ok(ApiResponse.success("Product deleted", null, elapsed(t)));
        }
        return ResponseEntity.status(404).body(ApiResponse.error("Product not found", elapsed(t)));
    }

    // Endpoint pesado: 10% de requests generan latencia extrema → ideal para ver p99/p99.5
    @GetMapping("/analytics")
    public ResponseEntity<ApiResponse<Map<String, Object>>> analytics() {
        long t = System.currentTimeMillis();
        Map<String, Object> data = productService.getAnalytics();
        return ResponseEntity.ok(ApiResponse.success("Analytics computed", data, elapsed(t)));
    }

    private long elapsed(long startMs) {
        return System.currentTimeMillis() - startMs;
    }
}