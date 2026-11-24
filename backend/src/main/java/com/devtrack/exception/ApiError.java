package com.devtrack.exception;

import java.time.LocalDateTime;
import java.util.Map;

public record ApiError(int status, String message, Map<String, String> fieldErrors, LocalDateTime timestamp) {
    public static ApiError of(int status, String message) {
        return new ApiError(status, message, Map.of(), LocalDateTime.now());
    }
}
