package com.twohands.admin_service.domain.enforcement;

import com.twohands.admin_service.exception.AppException;
import com.twohands.admin_service.exception.ErrorCode;

import java.time.Instant;

public final class UserEnforcementPolicy {

	private UserEnforcementPolicy() {
	}

	public static void validateSuspendRequest(String reasonCode, String description, Instant expiresAt, Instant now) {
		validateEnforcementRequest(reasonCode, description, expiresAt, now, "temporary suspend");
	}

	public static void validateBanRequest(String reasonCode, String description, Instant expiresAt, Instant now) {
		validateEnforcementRequest(reasonCode, description, expiresAt, now, "temporary ban");
	}

	public static void validateRestrictRequest(String reasonCode, String description, Instant expiresAt, Instant now) {
		validateEnforcementRequest(reasonCode, description, expiresAt, now, "temporary restrict");
	}

	public static void validateRevokeRequest(String note, String reason) {
		if (note != null && note.length() > 4000) {
			throw validationError("note", "note must be at most 4000 characters");
		}
		if (reason != null && reason.length() > 4000) {
			throw validationError("reason", "reason must be at most 4000 characters");
		}
	}

	public static void ensureRevocable(UserEnforcementStatus status) {
		if (status != UserEnforcementStatus.ACTIVE) {
			throw new AppException(
					ErrorCode.ENFORCEMENT_CONFLICT,
					"Only ACTIVE enforcement can be revoked"
			);
		}
	}

	private static void validateEnforcementRequest(
			String reasonCode,
			String description,
			Instant expiresAt,
			Instant now,
			String temporaryLabel
	) {
		if (reasonCode == null || reasonCode.isBlank()) {
			throw validationError("reason_code", "Reason code is required");
		}
		if (reasonCode.length() > 100) {
			throw validationError("reason_code", "Reason code must be at most 100 characters");
		}
		if (description == null || description.isBlank()) {
			throw validationError("description", "Description is required");
		}
		if (expiresAt != null && !expiresAt.isAfter(now)) {
			throw validationError("expires_at", "expires_at must be in the future for " + temporaryLabel);
		}
	}

	private static AppException validationError(String field, String reason) {
		return new AppException(ErrorCode.VALIDATION_ERROR, ErrorCode.VALIDATION_ERROR.defaultMessage(), field, reason);
	}
}
