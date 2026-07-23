package com.twohands.commerce_service.domain.order;

import com.twohands.commerce_service.domain.payment.PaymentMethod;
import com.twohands.commerce_service.domain.payment.PaymentStatus;
import com.twohands.commerce_service.domain.support.WebhookLogSupportQueryPolicy;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;

import java.time.Instant;
import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public final class OrderSupportListQueryPolicy {

    private static final Set<String> ALLOWED_PAYMENT_METHODS = Arrays.stream(PaymentMethod.values())
            .map(Enum::name)
            .collect(Collectors.toUnmodifiableSet());

    private static final Set<String> ALLOWED_PAYMENT_STATUSES = Arrays.stream(PaymentStatus.values())
            .map(Enum::name)
            .collect(Collectors.toUnmodifiableSet());

    private OrderSupportListQueryPolicy() {
    }

    public static Optional<OrderStatus> parseStatus(String raw) {
        if (raw == null || raw.isBlank()) {
            return Optional.empty();
        }
        try {
            return Optional.of(OrderStatus.valueOf(raw.trim().toUpperCase(Locale.ROOT)));
        } catch (IllegalArgumentException ex) {
            throw validationError("status", "status is not a valid order status");
        }
    }

    public static Optional<PaymentMethod> parsePaymentMethod(String raw) {
        if (raw == null || raw.isBlank()) {
            return Optional.empty();
        }
        String normalized = raw.trim().toUpperCase(Locale.ROOT);
        if (!ALLOWED_PAYMENT_METHODS.contains(normalized)) {
            throw validationError("payment_method", "payment_method must be one of " + ALLOWED_PAYMENT_METHODS);
        }
        return Optional.of(PaymentMethod.valueOf(normalized));
    }

    public static Optional<PaymentStatus> parsePaymentStatus(String raw) {
        if (raw == null || raw.isBlank()) {
            return Optional.empty();
        }
        String normalized = raw.trim().toUpperCase(Locale.ROOT);
        if (!ALLOWED_PAYMENT_STATUSES.contains(normalized)) {
            throw validationError("payment_status", "payment_status must be one of " + ALLOWED_PAYMENT_STATUSES);
        }
        return Optional.of(PaymentStatus.valueOf(normalized));
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

    public static OrderSupportListSortField parseSortField(String raw) {
        OrderSupportListSortField parsed = OrderSupportListSortField.fromQueryValue(raw);
        if (parsed == null) {
            throw validationError("sort", "sort must be one of: created_at, updated_at");
        }
        return parsed;
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
