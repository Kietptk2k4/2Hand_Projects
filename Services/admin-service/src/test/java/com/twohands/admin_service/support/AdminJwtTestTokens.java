package com.twohands.admin_service.support;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public final class AdminJwtTestTokens {

	private static final String TEST_SECRET = "test-access-secret-key-minimum-32-characters-123456";
	private static final SecretKey KEY = Keys.hmacShaKeyFor(TEST_SECRET.getBytes(StandardCharsets.UTF_8));

	private AdminJwtTestTokens() {
	}

	public static String accessToken(UUID userId, List<String> roles, List<String> permissions) {
		Instant now = Instant.now();
		return Jwts.builder()
				.subject(userId.toString())
				.claim("roles", roles)
				.claim("permissions", permissions)
				.issuedAt(Date.from(now))
				.expiration(Date.from(now.plusSeconds(3600)))
				.signWith(KEY)
				.compact();
	}
}
