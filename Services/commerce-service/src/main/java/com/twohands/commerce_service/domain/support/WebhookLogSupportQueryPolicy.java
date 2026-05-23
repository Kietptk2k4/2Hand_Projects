package com.twohands.commerce_service.domain.support;

import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;

import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.Set;

public final class WebhookLogSupportQueryPolicy {

    public static final Set<String> ALLOWED_PROVIDERS = Set.of("PAYOS", "GHN");
    public static final Set<String> ALLOWED_STATUSES = Set.of("PROCESSED", "PENDING", "INVALID_SIGNATURE");

    private WebhookLogSupportQueryPolicy() {
    }

    public static String normalizeProvider(String provider) {
        if (provider == null || provider.isBlank()) {
            return null;
        }
        String normalized = provider.trim().toUpperCase();
        if (!ALLOWED_PROVIDERS.contains(normalized)) {
            throw validationError("provider", "provider must be PAYOS or GHN");
        }
        return normalized;
    }

    public static String normalizeReferenceId(String referenceId) {
        if (referenceId == null || referenceId.isBlank()) {
            return null;
        }
        return referenceId.trim();
    }

    public static String normalizeProcessingStatus(String status) {
        if (status == null || status.isBlank()) {
            return null;
        }
        String normalized = status.trim().toUpperCase();
        if (!ALLOWED_STATUSES.contains(normalized)) {
            throw validationError("status", "status must be PROCESSED, PENDING, or INVALID_SIGNATURE");
        }
        return normalized;
    }

    public static Instant parseInstant(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Instant.parse(value.trim());
        } catch (DateTimeParseException ex) {
            throw validationError(fieldName, fieldName + " must be a valid ISO-8601 instant");
        }
    }

    public static void validateDateRange(Instant from, Instant to) {
        if (from != null && to != null && from.isAfter(to)) {
            throw validationError("from", "from must be before or equal to to");
        }
    }

    private static AppException validationError(String field, String reason) {
        return new AppException(ErrorCode.VALIDATION_ERROR, ErrorCode.VALIDATION_ERROR.defaultMessage(), field, reason);
    }
}
