package com.twohands.admin_service.infrastructure.auth;

import com.twohands.admin_service.domain.auth.AuthRoleGateway;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@ConditionalOnProperty(name = "admin.integrations.auth.enabled", havingValue = "false", matchIfMissing = true)
public class DisabledAuthRoleGateway implements AuthRoleGateway {

	@Override
	public boolean isEnabled() {
		return false;
	}

	@Override
	public boolean isAvailable() {
		return false;
	}

	@Override
	public List<String> resolveRoles(UUID adminId, String bearerToken) {
		return List.of();
	}
}
