package com.twohands.admin_service.security.session;

import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Profile("!test")
public class RedisAccessTokenInvalidationChecker implements AccessTokenInvalidationChecker {

	private static final String KEY_PREFIX = "auth:token:invalid-before:";

	private final StringRedisTemplate stringRedisTemplate;

	public RedisAccessTokenInvalidationChecker(StringRedisTemplate stringRedisTemplate) {
		this.stringRedisTemplate = stringRedisTemplate;
	}

	@Override
	public boolean isTokenInvalidated(UUID userId, long issuedAtEpochMilli) {
		if (userId == null) {
			return false;
		}

		String raw = stringRedisTemplate.opsForValue().get(KEY_PREFIX + userId);
		if (raw == null || raw.isBlank()) {
			return false;
		}

		try {
			long invalidBeforeMs = Long.parseLong(raw.trim());
			return issuedAtEpochMilli < invalidBeforeMs;
		} catch (NumberFormatException ex) {
			return false;
		}
	}
}
