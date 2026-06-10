package com.twohands.commerce_service.domain.shipment;

import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;

import java.util.Locale;
import java.util.Optional;

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

    private static AppException validationError(String field, String reason) {
        return new AppException(ErrorCode.VALIDATION_ERROR, ErrorCode.VALIDATION_ERROR.defaultMessage(), field, reason);
    }
}
