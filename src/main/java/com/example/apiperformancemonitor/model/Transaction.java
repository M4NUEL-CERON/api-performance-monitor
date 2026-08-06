package com.example.apiperformancemonitor.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Transaction {
    private Long id;
    private String type;       // CREDIT | DEBIT
    private BigDecimal amount;
    private String currency;
    private String status;     // PENDING | COMPLETED | FAILED
    private String description;
    private LocalDateTime timestamp;

    public Transaction() {}

    public Transaction(Long id, String type, BigDecimal amount, String currency,
                       String status, String description, LocalDateTime timestamp) {
        this.id = id;
        this.type = type;
        this.amount = amount;
        this.currency = currency;
        this.status = status;
        this.description = description;
        this.timestamp = timestamp;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}