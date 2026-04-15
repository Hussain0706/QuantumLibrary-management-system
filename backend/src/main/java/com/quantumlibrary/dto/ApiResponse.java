package com.quantumlibrary.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Generic API response wrapper for all REST endpoints.
 * Every response follows: { success, message, data }
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {

    private boolean success;
    private String message;
    private T data;

    /** Optional machine-readable error code for the frontend to branch on */
    private String errorCode;

    /** Convenience factory for successful responses */
    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .build();
    }

    /** Convenience factory for error responses */
    public static <T> ApiResponse<T> error(String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .data(null)
                .build();
    }

    /** Convenience factory for errors with a specific code the frontend can act on */
    public static <T> ApiResponse<T> errorWithCode(String message, String errorCode) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .errorCode(errorCode)
                .data(null)
                .build();
    }
}
