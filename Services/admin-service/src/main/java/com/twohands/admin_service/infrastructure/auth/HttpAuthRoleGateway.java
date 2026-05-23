package com.twohands.admin_service.infrastructure.auth;

import com.twohands.admin_service.domain.auth.AuthRoleGateway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * MVP: Auth Service chưa expose GET user roles; gateway sẵn sàng mở rộng.
 * Roles lấy từ JWT claims qua {@link com.twohands.admin_service.application.auth.JwtClaimsAdminAuthorizationService}.
 */
@Component
@ConditionalOnProperty(name = "admin.integrations.auth.enabled", havingValue = "true")
public class HttpAuthRoleGateway implements AuthRoleGateway {

	private static final Logger log = LoggerFactory.getLogger(HttpAuthRoleGateway.class);

	@Override
	public boolean isEnabled() {
		return true;
	}

	@Override
	public boolean isAvailable() {
		return true;
	}

	@Override
	public List<String> resolveRoles(UUID adminId, String bearerToken) {
		log.debug("Auth role HTTP lookup not implemented for adminId={}; use JWT claims", adminId);
		return List.of();
	}
}
