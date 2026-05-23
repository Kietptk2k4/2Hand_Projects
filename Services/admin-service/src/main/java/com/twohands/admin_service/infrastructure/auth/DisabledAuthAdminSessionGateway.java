package com.twohands.admin_service.infrastructure.auth;

import com.twohands.admin_service.domain.auth.AdminSessionRevokeRequest;
import com.twohands.admin_service.domain.auth.AdminSessionRevokeResult;
import com.twohands.admin_service.domain.auth.AuthAdminSessionGateway;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "admin.integrations.auth.enabled", havingValue = "false", matchIfMissing = true)
public class DisabledAuthAdminSessionGateway implements AuthAdminSessionGateway {

	@Override
	public boolean isEnabled() {
		return false;
	}

	@Override
	public AdminSessionRevokeResult revoke(AdminSessionRevokeRequest request) {
		throw new UnsupportedOperationException("Auth admin session integration is disabled");
	}
}
