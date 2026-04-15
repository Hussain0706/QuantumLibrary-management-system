package com.quantumlibrary.config;

import com.quantumlibrary.dto.ApiResponse;
import com.quantumlibrary.exception.BorrowLimitException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * GlobalExceptionHandler — translates Java exceptions into clean JSON error responses.
 *
 *  All errors follow:  { "success": false, "message": "...", "data": null }
 *
 *  Handles:
 *   - BadCredentialsException  → 401 Unauthorized
 *   - IllegalArgumentException → 400 Bad Request
 *   - IllegalStateException    → 409 Conflict
 *   - ValidationException      → 400 Bad Request (with field name)
 *   - Everything else          → 500 Internal Server Error
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse<Void>> handleBadCredentials(BadCredentialsException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error(e.getMessage()));
    }

    /**
     * Borrow limit reached — returns errorCode so the frontend can show
     * an interactive "Return a book to continue" prompt instead of a plain error.
     */
    @ExceptionHandler(BorrowLimitException.class)
    public ResponseEntity<ApiResponse<Void>> handleBorrowLimit(BorrowLimitException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.errorWithCode(e.getMessage(), "BORROW_LIMIT_REACHED"));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArg(IllegalArgumentException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(e.getMessage()));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalState(IllegalStateException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.error(e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldErrors()
                .stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .findFirst()
                .orElse("Validation failed");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(msg));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleAll(Exception e) {
        log.error("💥 Unhandled exception: ", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Server error: " + e.getMessage()));
    }
}
