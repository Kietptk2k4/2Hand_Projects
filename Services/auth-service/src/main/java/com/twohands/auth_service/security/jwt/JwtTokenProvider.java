package com.twohands.auth_service.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Component
public class JwtTokenProvider {

    private final SecretKey accessSecretKey;

    public JwtTokenProvider(@Value("${jwt.access-secret}") String accessSecret) {
        this.accessSecretKey = Keys.hmacShaKeyFor(accessSecret.getBytes(StandardCharsets.UTF_8));
    }

    public String getSubject(String token) {
        return parseClaims(token).getSubject();
    }

    public String getEmail(String token) {
        Object email = parseClaims(token).get("email");
        return email == null ? null : String.valueOf(email);
    }

    public String getStatus(String token) {
        Object status = parseClaims(token).get("status");
        return status == null ? null : String.valueOf(status);
    }

    public long getExpiresInSeconds(String token) {
        Date expiration = parseClaims(token).getExpiration();
        if (expiration == null) {
            return 0L;
        }
        return Math.max(0L, expiration.toInstant().getEpochSecond() - Instant.now().getEpochSecond());
    }

    public Collection<? extends GrantedAuthority> getAuthorities(String token) {
        Object rawRoles = parseClaims(token).get("roles");
        if (!(rawRoles instanceof List<?> roles)) {
            return Collections.emptyList();
        }

        return roles.stream()
                .map(String::valueOf)
                .map(role -> role.startsWith("ROLE_") ? role : "ROLE_" + role)
                .map(SimpleGrantedAuthority::new)
                .toList();
    }

    public boolean isValid(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(accessSecretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
