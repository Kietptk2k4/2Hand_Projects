package com.twohands.admin_service.domain.audit;

import com.twohands.admin_service.exception.AppException;
import com.twohands.admin_service.exception.ErrorCode;

import java.time.Instant;
import java.time.format.DateTimeParseException;

public final class AdminActionLogQueryPolicy {

	private AdminActionLogQueryPolicy() {
	}

	public static void validateDateRange(Instant from, Instant to) {
		if (from != null && to != null && from.isAfter(to)) {
			throw new AppException(
					ErrorCode.VALIDATION_ERROR,
					"Invalid date range: from must be before or equal to to",
					"from",
					"must be before or equal to to"
			);
		}
	}

	public static AdminActionStatus parseStatus(String status) {
		if (status == null || status.isBlank()) {
			return null;
		}
		try {
			return AdminActionStatus.valueOf(status.trim().toUpperCase());
		} catch (IllegalArgumentException ex) {
			throw new AppException(
					ErrorCode.VALIDATION_ERROR,
					"status must be SUCCESS or FAILURE",
					"status",
					"invalid value"
			);
		}
	}

	public static String normalizeTargetType(String targetType) {
		String normalized = normalizeOptionalText(targetType);
		return normalized == null ? null : normalized.toUpperCase();
	}

	public static String normalizeActionType(String action) {
		String normalized = normalizeOptionalText(action);
		return normalized == null ? null : normalized.toUpperCase();
	}

	public static String normalizeOptionalText(String value) {
		if (value == null) {
			return null;
		}
		String trimmed = value.trim();
		return trimmed.isEmpty() ? null : trimmed;
	}

	public static Instant parseInstant(String value, String fieldName) {
		if (value == null || value.isBlank()) {
			return null;
		}
		try {
			return Instant.parse(value.trim());
		} catch (DateTimeParseException ex) {
			throw new AppException(
					ErrorCode.VALIDATION_ERROR,
					fieldName + " must be a valid ISO-8601 instant",
					fieldName,
					"invalid format"
			);
		}
	}
}
