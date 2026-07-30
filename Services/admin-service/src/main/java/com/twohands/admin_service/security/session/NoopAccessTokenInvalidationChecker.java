package com.twohands.admin_service.security.session;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Profile("test")
public class NoopAccessTokenInvalidationChecker implements AccessTokenInvalidationChecker {

	@Override
	public boolean isTokenInvalidated(UUID userId, long issuedAtEpochMilli) {
		return false;
	}
}
