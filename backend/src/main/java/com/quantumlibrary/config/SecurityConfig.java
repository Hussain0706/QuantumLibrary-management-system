package com.quantumlibrary.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * SecurityConfig — Spring Security configuration.
 *
 *  ✅ Stateless JWT (no sessions)
 *  ✅ CORS configured to allow all origins (development mode for file:// frontend)
 *  ✅ CSRF disabled (not needed for stateless REST APIs)
 *  ✅ H2 console access enabled (in-memory DB debugging)
 *  ✅ Method-level security via @PreAuthorize
 *
 *  Public endpoints: /api/auth/**, /api/books (GET), /h2-console/**
 *  Admin only: /api/admin/**
 *  All else: requires valid JWT
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // ── Disable CSRF (stateless REST API doesn't need it) ──
            .csrf(AbstractHttpConfigurer::disable)

            // ── CORS (allow frontend file:// and localhost calls) ──
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))

            // ── Stateless session (JWT handles auth, no server sessions) ──
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            // ── Endpoint access rules ──
            .authorizeHttpRequests(auth -> auth
                // Public: auth endpoints
                .requestMatchers("/api/auth/**").permitAll()
                // Public: read books (borrowing requires auth)
                .requestMatchers("GET", "/api/books/**").permitAll()
                .requestMatchers("GET", "/api/books").permitAll()
                // H2 database console
                .requestMatchers("/h2-console/**").permitAll()
                // Admin-only routes (also enforced by @PreAuthorize)
                .requestMatchers("/api/admin/**").hasAuthority("ROLE_ADMIN")
                // Everything else requires authentication
                .anyRequest().authenticated()
            )

            // ── JWT filter runs before Spring's auth filter ──
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)

            // ── Allow H2 console iframes ──
            .headers(h -> h.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable));

        return http.build();
    }

    /** BCrypt password encoder — strength 10 (default) */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * CORS configuration.
     * Allows all origins so the frontend HTML files (served from file:// or
     * any local server) can call the backend during development.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(List.of("*"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        config.setAllowedHeaders(List.of("*"));
        config.setExposedHeaders(List.of("Authorization"));
        config.setAllowCredentials(false);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
