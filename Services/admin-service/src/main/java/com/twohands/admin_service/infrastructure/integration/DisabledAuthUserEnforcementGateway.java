package com.twohands.admin_service.infrastructure.integration;

import com.twohands.admin_service.domain.integration.AuthUserEnforcementGateway;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "admin.integrations.auth.enabled", havingValue = "false", matchIfMissing = true)
public class DisabledAuthUserEnforcementGateway implements AuthUserEnforcementGateway {

	@Override
	public boolean isEnabled() {
		return false;
	}

	@Override
	public void suspendUser(AuthSuspendUserRequest request) {
		// Outbox event USER_SUSPENDED applies Auth effect asynchronously.
	}

	@Override
	public void banUser(AuthBanUserRequest request) {
		// Outbox event USER_BANNED applies Auth effect asynchronously.
	}
}
