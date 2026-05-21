package com.twohands.admin_service.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Component
public class JwtTokenProvider {

	private final SecretKey accessSecretKey;

	public JwtTokenProvider(@Value("${jwt.access-secret}") String accessSecret) {
		this.accessSecretKey = Keys.hmacShaKeyFor(accessSecret.getBytes(StandardCharsets.UTF_8));
	}

	public UUID getUserId(String token) {
		Claims claims = parseClaims(token);
		String subject = claims.getSubject();
		if (subject == null) {
			return null;
		}
		return UUID.fromString(subject);
	}

	public List<String> getRoles(String token) {
		return readStringList(parseClaims(token).get("roles"));
	}

	public List<String> getPermissions(String token) {
		return readStringList(parseClaims(token).get("permissions"));
	}

	public boolean isValid(String token) {
		try {
			parseClaims(token);
			return true;
		} catch (Exception ex) {
			return false;
		}
	}

	private List<String> readStringList(Object raw) {
		if (!(raw instanceof List<?> values)) {
			return Collections.emptyList();
		}
		return values.stream().map(String::valueOf).toList();
	}

	private Claims parseClaims(String token) {
		return Jwts.parser()
				.verifyWith(accessSecretKey)
				.build()
				.parseSignedClaims(token)
				.getPayload();
	}
}
