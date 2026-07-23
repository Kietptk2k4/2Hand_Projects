package com.twohands.commerce_service.domain.shipment;

import com.twohands.commerce_service.domain.support.WebhookLogSupportQueryPolicy;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;

import java.time.Instant;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

public final class ShipmentSupportListQueryPolicy {

    private ShipmentSupportListQueryPolicy() {
    }

    public static Optional<ShipmentStatus> parseStatus(String raw) {
        if (raw == null || raw.isBlank()) {
            return Optional.empty();
        }
        try {
            return Optional.of(ShipmentStatus.valueOf(raw.trim().toUpperCase(Locale.ROOT)));
        } catch (IllegalArgumentException ex) {
            throw validationError("status", "status is not a valid shipment status");
        }
    }

    public static Optional<ShipmentCarrier> parseCarrier(String raw) {
        if (raw == null || raw.isBlank()) {
            return Optional.empty();
        }
        try {
            return Optional.of(ShipmentCarrier.valueOf(raw.trim().toUpperCase(Locale.ROOT)));
        } catch (IllegalArgumentException ex) {
            throw validationError("carrier", "carrier is not a valid shipment carrier");
        }
    }

    public static ShipmentSupportListSortField parseSortField(String raw) {
        ShipmentSupportListSortField parsed = ShipmentSupportListSortField.fromQueryValue(raw);
        if (parsed == null) {
            throw validationError("sort", "sort must be one of: updated_at, created_at, shipped_at");
        }
        return parsed;
    }

    public static Optional<String> parseSearchQuery(String raw) {
        if (raw == null || raw.isBlank()) {
            return Optional.empty();
        }
        String trimmed = raw.trim();
        if (trimmed.isEmpty()) {
            return Optional.empty();
        }
        if (!trimmed.matches("[0-9a-zA-Z-]+")) {
            throw validationError("q", "q contains invalid characters");
        }
        return Optional.of(trimmed);
    }

    public static Optional<UUID> parseOrderId(String raw) {
        if (raw == null || raw.isBlank()) {
            return Optional.empty();
        }
        try {
            return Optional.of(UUID.fromString(raw.trim()));
        } catch (IllegalArgumentException ex) {
            throw validationError("order_id", "order_id must be a valid UUID");
        }
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
