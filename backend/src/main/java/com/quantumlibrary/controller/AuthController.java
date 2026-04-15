package com.quantumlibrary.controller;

import com.quantumlibrary.dto.ApiResponse;
import com.quantumlibrary.dto.LoginRequest;
import com.quantumlibrary.dto.LoginResponse;
import com.quantumlibrary.dto.RegisterRequest;
import com.quantumlibrary.entity.User;
import com.quantumlibrary.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * AuthController — public endpoints (no JWT required)
 *
 *  POST /api/auth/login    → returns JWT token
 *  POST /api/auth/register → creates new member account
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /** Login and receive a JWT token */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest req) {
        LoginResponse resp = authService.login(req);
        return ResponseEntity.ok(ApiResponse.success("Login successful", resp));
    }

    /** Member self-registration */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<String>> register(
            @Valid @RequestBody RegisterRequest req) {
        User user = authService.register(req);
        return ResponseEntity.ok(ApiResponse.success(
            "Account created! Welcome email sent to " + user.getEmail(), null));
    }
}
