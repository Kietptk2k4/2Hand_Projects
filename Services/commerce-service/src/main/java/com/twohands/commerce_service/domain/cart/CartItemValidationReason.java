package com.twohands.commerce_service.domain.cart;

public enum CartItemValidationReason {
    CART_ITEM_REMOVED("Cart item was removed"),
    INVALID_PRODUCT("Product or shop is no longer available"),
    OUT_OF_STOCK("Insufficient stock for requested quantity"),
    PRODUCT_NOT_ACTIVE("Product is not active"),
    SHOP_NOT_ACTIVE("Shop is not active"),
    CATEGORY_INACTIVE("Product category is inactive"),
    ACTIVE_PRICE_MISSING("Active product price is missing"),
    SHOP_ON_VACATION("Shop is on vacation"),
    PRODUCT_NOT_FOUND("Product not found");

    private final String message;

    CartItemValidationReason(String message) {
        this.message = message;
    }

    public String code() {
        return name();
    }

    public String message() {
        return message;
    }
}
