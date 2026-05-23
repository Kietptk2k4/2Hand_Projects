package com.twohands.admin_service.infrastructure.integration;

import com.twohands.admin_service.domain.integration.AuthUserLookupGateway;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@ConditionalOnProperty(name = "admin.integrations.auth.enabled", havingValue = "false", matchIfMissing = true)
public class DisabledAuthUserLookupGateway implements AuthUserLookupGateway {

	@Override
	public boolean isEnabled() {
		return false;
	}

	@Override
	public void ensureUserExists(UUID userId) {
		// User existence is validated when Auth integration is enabled.
	}
}
