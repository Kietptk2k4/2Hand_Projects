package com.twohands.admin_service.domain.auth;

import java.util.List;
import java.util.UUID;

/**
 * Optional Auth Service role lookup when JWT claims are insufficient.
 */
public interface AuthRoleGateway {

	boolean isEnabled();

	boolean isAvailable();

	List<String> resolveRoles(UUID adminId, String bearerToken);
}
