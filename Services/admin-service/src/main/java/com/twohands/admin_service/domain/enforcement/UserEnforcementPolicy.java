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
