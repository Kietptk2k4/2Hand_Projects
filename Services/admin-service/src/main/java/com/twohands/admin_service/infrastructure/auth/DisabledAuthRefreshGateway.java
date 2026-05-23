package com.twohands.admin_service.infrastructure.auth;

import com.twohands.admin_service.domain.auth.AdminRefreshTokenRequest;
import com.twohands.admin_service.domain.auth.AdminRefreshedAccessToken;
import com.twohands.admin_service.domain.auth.AuthRefreshGateway;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "admin.integrations.auth.enabled", havingValue = "false", matchIfMissing = true)
public class DisabledAuthRefreshGateway implements AuthRefreshGateway {

	@Override
	public boolean isEnabled() {
		return false;
	}

	@Override
	public AdminRefreshedAccessToken refresh(AdminRefreshTokenRequest request) {
		throw new UnsupportedOperationException("Auth refresh integration is disabled");
	}
}
