package com.example.apiperformancemonitor.model;

import java.time.LocalDateTime;

public class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data;
    private LocalDateTime timestamp;
    private long processingTimeMs;

    private ApiResponse() {}

    public static <T> ApiResponse<T> success(String message, T data, long processingTimeMs) {
        ApiResponse<T> r = new ApiResponse<>();
        r.success = true;
        r.message = message;
        r.data = data;
        r.timestamp = LocalDateTime.now();
        r.processingTimeMs = processingTimeMs;
        return r;
    }

    public static <T> ApiResponse<T> error(String message, long processingTimeMs) {
        ApiResponse<T> r = new ApiResponse<>();
        r.success = false;
        r.message = message;
        r.timestamp = LocalDateTime.now();
        r.processingTimeMs = processingTimeMs;
        return r;
    }

    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public T getData() { return data; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public long getProcessingTimeMs() { return processingTimeMs; }
}