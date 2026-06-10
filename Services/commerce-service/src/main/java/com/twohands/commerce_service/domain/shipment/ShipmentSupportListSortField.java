package com.twohands.commerce_service.domain.shipment;

public enum ShipmentSupportListSortField {
    UPDATED_AT("updated_at"),
    CREATED_AT("created_at"),
    SHIPPED_AT("shipped_at");

    private final String queryValue;

    ShipmentSupportListSortField(String queryValue) {
        this.queryValue = queryValue;
    }

    public String queryValue() {
        return queryValue;
    }

    public static ShipmentSupportListSortField fromQueryValue(String raw) {
        if (raw == null || raw.isBlank()) {
            return UPDATED_AT;
        }
        String normalized = raw.trim().toLowerCase();
        for (ShipmentSupportListSortField field : values()) {
            if (field.queryValue.equals(normalized)) {
                return field;
            }
        }
        return null;
    }
}
