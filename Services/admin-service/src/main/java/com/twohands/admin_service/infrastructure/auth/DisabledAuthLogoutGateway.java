package com.twohands.admin_service.infrastructure.auth;

import com.twohands.admin_service.domain.auth.AdminLogoutDelegation;
import com.twohands.admin_service.domain.auth.AuthLogoutGateway;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "admin.integrations.auth.enabled", havingValue = "false", matchIfMissing = true)
public class DisabledAuthLogoutGateway implements AuthLogoutGateway {

	@Override
	public boolean isEnabled() {
		return false;
	}

	@Override
	public void logout(AdminLogoutDelegation delegation) {
		throw new UnsupportedOperationException("Auth logout integration is disabled");
	}
}
