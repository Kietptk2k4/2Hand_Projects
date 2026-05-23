package com.twohands.admin_service.infrastructure.auth;

import com.twohands.admin_service.domain.auth.AuthPermissionGateway;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@ConditionalOnProperty(name = "admin.integrations.auth.enabled", havingValue = "false", matchIfMissing = true)
public class DisabledAuthPermissionGateway implements AuthPermissionGateway {

	@Override
	public boolean isEnabled() {
		return false;
	}

	@Override
	public boolean isAvailable() {
		return false;
	}

	@Override
	public boolean hasPermission(UUID adminId, String permissionCode, String bearerToken) {
		return false;
	}
}
