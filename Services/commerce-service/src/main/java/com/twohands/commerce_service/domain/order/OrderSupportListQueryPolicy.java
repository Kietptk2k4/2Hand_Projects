package com.twohands.commerce_service.domain.order;

import com.twohands.commerce_service.domain.payment.PaymentMethod;
import com.twohands.commerce_service.domain.support.WebhookLogSupportQueryPolicy;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;

import java.time.Instant;
import java.util.Locale;
import java.util.Optional;

public final class OrderSupportListQueryPolicy {

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
        try {
            return Optional.of(PaymentMethod.valueOf(raw.trim().toUpperCase(Locale.ROOT)));
        } catch (IllegalArgumentException ex) {
            throw validationError("payment_method", "payment_method must be COD or PAYOS");
        }
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
