package com.twohands.admin_service.domain.integration;

import java.time.Instant;
import java.util.UUID;

public interface AuthUserEnforcementGateway {

	boolean isEnabled();

	void suspendUser(AuthSuspendUserRequest request);

	void banUser(AuthBanUserRequest request);

	void restrictUser(AuthRestrictUserRequest request);

	void revokeEnforcement(AuthRevokeEnforcementRequest request);

	record AuthSuspendUserRequest(
			UUID userId,
			UUID enforcementId,
			String reasonCode,
			String description,
			Instant expiresAt,
			String bearerToken
	) {
	}

	record AuthBanUserRequest(
			UUID userId,
			UUID enforcementId,
			String reasonCode,
			String description,
			Instant expiresAt,
			String bearerToken
	) {
	}

	record AuthRestrictUserRequest(
			UUID userId,
			UUID enforcementId,
			String reasonCode,
			String description,
			Instant expiresAt,
			String bearerToken
	) {
	}

	record AuthRevokeEnforcementRequest(
			UUID enforcementId,
			UUID userId,
			String actionType,
			boolean reactivateUser,
			String note,
			String reason,
			String bearerToken
	) {
	}
}
