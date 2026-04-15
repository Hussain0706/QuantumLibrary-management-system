package com.quantumlibrary.service;

import com.quantumlibrary.config.JwtUtil;
import com.quantumlibrary.dto.LoginRequest;
import com.quantumlibrary.dto.LoginResponse;
import com.quantumlibrary.dto.RegisterRequest;
import com.quantumlibrary.entity.User;
import com.quantumlibrary.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * AuthService — handles login and member registration.
 *
 *  POST /api/auth/login    → validates credentials, returns JWT token
 *  POST /api/auth/register → creates member account, sends welcome email
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final EmailService emailService;

    /**
     * Validates email + password, checks account is active,
     * then returns a signed JWT token with role + userId claims.
     */
    public LoginResponse login(LoginRequest req) {
        User user = userRepository.findByEmail(req.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            throw new BadCredentialsException("Invalid email or password");
        }

        if (!user.isActive()) {
            throw new BadCredentialsException(
                "Your account has been deactivated. Please contact the library admin.");
        }

        String token = jwtUtil.generateToken(user);
        log.info("🔑 Login successful: {} ({})", user.getEmail(), user.getRole());

        return LoginResponse.builder()
                .token(token)
                .role(user.getRole().name())
                .name(user.getName())
                .email(user.getEmail())
                .id(user.getId())
                .build();
    }

    /**
     * Creates a new ROLE_MEMBER account.
     * Sends a welcome email asynchronously after save.
     */
    public User register(RegisterRequest req) {
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new IllegalArgumentException(
                "An account already exists for: " + req.getEmail());
        }

        User user = User.builder()
                .name(req.getName())
                .email(req.getEmail())
                .password(passwordEncoder.encode(req.getPassword()))
                .phone(req.getPhone())
                .role(User.Role.ROLE_MEMBER)
                .active(true)
                .build();

        User saved = userRepository.save(user);
        log.info("✅ New member registered: {}", saved.getEmail());

        // Welcome email (async — does not block response)
        emailService.sendWelcomeEmail(saved);

        return saved;
    }
}
