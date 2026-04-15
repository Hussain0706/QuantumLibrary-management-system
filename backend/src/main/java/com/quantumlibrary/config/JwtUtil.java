package com.quantumlibrary.config;

import com.quantumlibrary.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JwtUtil — JWT token generation and validation.
 *
 *  Tokens contain:
 *    sub   = user email
 *    role  = ROLE_ADMIN or ROLE_MEMBER
 *    userId = database user ID
 *    name  = display name
 *    iat   = issued-at timestamp
 *    exp   = expiry (default 24 hours)
 *
 *  Signed with HMAC-SHA256 using the configured secret key.
 */
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expiration;

    private SecretKey getKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /** Generate a signed JWT token for the given user */
    public String generateToken(User user) {
        return Jwts.builder()
                .subject(user.getEmail())
                .claim("role",   user.getRole().name())
                .claim("userId", user.getId())
                .claim("name",   user.getName())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getKey())
                .compact();
    }

    /** Parse and return all claims from the token */
    public Claims extractClaims(String token) {
        return Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /** Extract the email (subject) from the token */
    public String extractEmail(String token) {
        return extractClaims(token).getSubject();
    }

    /** Returns true if the token is valid (signature + not expired) */
    public boolean validateToken(String token) {
        try {
            extractClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}
