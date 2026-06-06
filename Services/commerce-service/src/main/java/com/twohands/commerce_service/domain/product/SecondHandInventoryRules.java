package com.twohands.commerce_service.domain.product;

public final class SecondHandInventoryRules {

    public static final int MAX_LISTING_STOCK = 1;

    private SecondHandInventoryRules() {
    }

    public static boolean isAllowedStockQuantity(int stockQuantity) {
        return stockQuantity >= 0 && stockQuantity <= MAX_LISTING_STOCK;
    }

    public static boolean isAllowedLowStockThreshold(int lowStockThreshold) {
        return lowStockThreshold >= 0 && lowStockThreshold <= MAX_LISTING_STOCK;
    }
}