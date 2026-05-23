package com.twohands.auth_service.security.jwt;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@Component
public class JwtTokenIssuer {

    private final SecretKey accessSecretKey;
    private final long accessExpirationMillis;
    private final long refreshExpirationMillis;
    private final SecureRandom secureRandom = new SecureRandom();

    public JwtTokenIssuer(
            @Value("${jwt.access-secret}") String accessSecret,
            @Value("${jwt.access-expiration:900000}") long accessExpirationMillis,
            @Value("${jwt.refresh-expiration:2592000000}") long refreshExpirationMillis
    ) {
        this.accessSecretKey = Keys.hmacShaKeyFor(accessSecret.getBytes(StandardCharsets.UTF_8));
        this.accessExpirationMillis = accessExpirationMillis;
        this.refreshExpirationMillis = refreshExpirationMillis;
    }

    public TokenPair issue(UUID userId, String email, String status, Instant now) {
        Instant accessExpiresAt = now.plusMillis(accessExpirationMillis);
        Instant refreshExpiresAt = now.plusMillis(refreshExpirationMillis);

        String accessToken = Jwts.builder()
                .subject(userId.toString())
                .claim("email", email)
                .claim("status", status)
                .claim("roles", List.of("USER"))
                .issuedAt(java.util.Date.from(now))
                .expiration(java.util.Date.from(accessExpiresAt))
                .signWith(accessSecretKey)
                .compact();

        String refreshToken = generateOpaqueRefreshToken();
        return new TokenPair(accessToken, refreshToken, accessExpiresAt, refreshExpiresAt, accessExpirationMillis / 1000);
    }

    public TokenPair issueAdminAccess(
            UUID userId,
            String email,
            String status,
            List<String> roles,
            List<String> permissions,
            Instant now
    ) {
        Instant accessExpiresAt = now.plusMillis(accessExpirationMillis);
        Instant refreshExpiresAt = now.plusMillis(refreshExpirationMillis);

        String accessToken = Jwts.builder()
                .subject(userId.toString())
                .claim("email", email)
                .claim("status", status)
                .claim("roles", roles == null || roles.isEmpty() ? List.of() : roles)
                .claim("permissions", permissions == null || permissions.isEmpty() ? List.of() : permissions)
                .issuedAt(java.util.Date.from(now))
                .expiration(java.util.Date.from(accessExpiresAt))
                .signWith(accessSecretKey)
                .compact();

        String refreshToken = generateOpaqueRefreshToken();
        return new TokenPair(accessToken, refreshToken, accessExpiresAt, refreshExpiresAt, accessExpirationMillis / 1000);
    }

    public AccessTokenOnly issueAccessToken(UUID userId, String email, String status, Instant now) {
        Instant accessExpiresAt = now.plusMillis(accessExpirationMillis);
        String accessToken = Jwts.builder()
                .subject(userId.toString())
                .claim("email", email)
                .claim("status", status)
                .claim("roles", List.of("USER"))
                .issuedAt(java.util.Date.from(now))
                .expiration(java.util.Date.from(accessExpiresAt))
                .signWith(accessSecretKey)
                .compact();
        return new AccessTokenOnly(accessToken, accessExpiresAt, accessExpirationMillis / 1000);
    }

    private String generateOpaqueRefreshToken() {
        byte[] bytes = new byte[48];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    public record TokenPair(
            String accessToken,
            String refreshToken,
            Instant accessExpiresAt,
            Instant refreshExpiresAt,
            long accessExpiresInSeconds
    ) {
    }

    public record AccessTokenOnly(
            String accessToken,
            Instant accessExpiresAt,
            long accessExpiresInSeconds
    ) {
    }
}
