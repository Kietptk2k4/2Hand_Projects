package com.twohands.commerce_service.domain.order;

public enum OrderSupportListSortField {
    CREATED_AT("created_at"),
    UPDATED_AT("updated_at");

    private final String queryValue;

    OrderSupportListSortField(String queryValue) {
        this.queryValue = queryValue;
    }

    public String queryValue() {
        return queryValue;
    }

    public static OrderSupportListSortField fromQueryValue(String raw) {
        if (raw == null || raw.isBlank()) {
            return CREATED_AT;
        }
        String normalized = raw.trim().toLowerCase();
        for (OrderSupportListSortField field : values()) {
            if (field.queryValue.equals(normalized)) {
                return field;
            }
        }
        return null;
    }
}
