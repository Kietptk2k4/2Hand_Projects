package com.twohands.admin_service.infrastructure.auth;

import com.twohands.admin_service.domain.auth.AdminCredentialLogin;
import com.twohands.admin_service.domain.auth.AdminLoginTokens;
import com.twohands.admin_service.domain.auth.AuthLoginGateway;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "admin.integrations.auth.enabled", havingValue = "false", matchIfMissing = true)
public class DisabledAuthLoginGateway implements AuthLoginGateway {

	@Override
	public boolean isEnabled() {
		return false;
	}

	@Override
	public AdminLoginTokens login(AdminCredentialLogin login) {
		throw new UnsupportedOperationException("Auth login integration is disabled");
	}
}
