package com.quantumlibrary.config;

import com.quantumlibrary.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * JwtFilter — intercepts every HTTP request and extracts JWT credentials.
 *
 *  Flow:
 *   1. Read "Authorization: Bearer <token>" header
 *   2. Validate the JWT signature and expiry
 *   3. Load user from DB using the email in the token
 *   4. Set Spring Security authentication context
 *
 *  If the token is missing or invalid, the request proceeds unauthenticated
 *  (public endpoints will still work; protected endpoints return 403).
 */
@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil        jwtUtil;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest  request,
                                    HttpServletResponse response,
                                    FilterChain         chain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);

            if (jwtUtil.validateToken(token)) {
                String email = jwtUtil.extractEmail(token);
                String role  = jwtUtil.extractClaims(token).get("role", String.class);

                userRepository.findByEmail(email).ifPresent(user -> {
                    var auth = new UsernamePasswordAuthenticationToken(
                            user, null,
                            List.of(new SimpleGrantedAuthority(role))
                    );
                    SecurityContextHolder.getContext().setAuthentication(auth);
                });
            }
        }

        chain.doFilter(request, response);
    }
}
