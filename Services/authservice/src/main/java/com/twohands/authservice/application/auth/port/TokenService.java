package com.twohands.authservice.application.auth.port;

import java.util.List;
import java.util.UUID;

public interface TokenService {

    String issueAccessToken(UUID userId, String email, List<String> roles);

    long getAccessTokenExpirationSeconds();

    /**
     * Parses a JWT access token and extracts the subject (userId).
     * Throws an exception if the token is invalid or expired.
     */
    UUID extractUserId(String token);
}
