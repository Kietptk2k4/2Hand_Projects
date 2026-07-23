package com.twohands.commerce_service.domain.payment;

import com.twohands.commerce_service.domain.support.WebhookLogSupportQueryPolicy;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;

import java.time.Instant;
import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public final class PaymentSupportQueryPolicy {

    private static final Set<String> ALLOWED_STATUSES = Arrays.stream(PaymentStatus.values())
            .map(Enum::name)
            .collect(Collectors.toUnmodifiableSet());

    private static final Set<String> ALLOWED_METHODS = Arrays.stream(PaymentMethod.values())
            .map(Enum::name)
            .collect(Collectors.toUnmodifiableSet());

    private static final Set<String> ALLOWED_RECONCILIATION_STATUSES = Arrays.stream(PaymentSupportReconciliationStatus.values())
            .map(Enum::name)
            .collect(Collectors.toUnmodifiableSet());

    private PaymentSupportQueryPolicy() {
    }

    public static PaymentStatus normalizeStatus(String status) {
        if (status == null || status.isBlank()) {
            return null;
        }
        String normalized = status.trim().toUpperCase();
        if (!ALLOWED_STATUSES.contains(normalized)) {
            throw validationError("status", "status must be one of " + ALLOWED_STATUSES);
        }
        return PaymentStatus.valueOf(normalized);
    }

    public static PaymentMethod normalizePaymentMethod(String paymentMethod) {
        if (paymentMethod == null || paymentMethod.isBlank()) {
            return null;
        }
        String normalized = paymentMethod.trim().toUpperCase();
        if (!ALLOWED_METHODS.contains(normalized)) {
            throw validationError("payment_method", "payment_method must be one of " + ALLOWED_METHODS);
        }
        return PaymentMethod.valueOf(normalized);
    }

    public static UUID normalizeOrderId(String orderId) {
        if (orderId == null || orderId.isBlank()) {
            return null;
        }
        try {
            return UUID.fromString(orderId.trim());
        } catch (IllegalArgumentException ex) {
            throw validationError("order_id", "order_id must be a valid UUID");
        }
    }

    public static Optional<String> parseSearchQuery(String raw) {
        if (raw == null || raw.isBlank()) {
            return Optional.empty();
        }
        String trimmed = raw.trim();
        if (trimmed.isEmpty()) {
            return Optional.empty();
        }
        if (!trimmed.matches("[0-9a-fA-F-]+")) {
            throw validationError("q", "q must contain only hexadecimal UUID characters");
        }
        return Optional.of(trimmed);
    }

    public static PaymentSupportReconciliationStatus normalizeReconciliationStatus(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        String normalized = raw.trim().toUpperCase(Locale.ROOT);
        if (!ALLOWED_RECONCILIATION_STATUSES.contains(normalized)) {
            throw validationError(
                    "reconciliation_status",
                    "reconciliation_status must be one of " + ALLOWED_RECONCILIATION_STATUSES
            );
        }
        return PaymentSupportReconciliationStatus.valueOf(normalized);
    }

    public static Instant parseInstant(String value, String fieldName) {
        return WebhookLogSupportQueryPolicy.parseInstant(value, fieldName);
    }

    public static void validateDateRange(Instant from, Instant to) {
        WebhookLogSupportQueryPolicy.validateDateRange(from, to);
    }

    private static AppException validationError(String field, String reason) {
        return new AppException(ErrorCode.VALIDATION_ERROR, ErrorCode.VALIDATION_ERROR.defaultMessage(), field, reason);
    }
}
