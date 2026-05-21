package com.twohands.commerce_service.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
    INTERNAL_ERROR("COMMERCE-500", HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error"),
    BAD_REQUEST("COMMERCE-400", HttpStatus.BAD_REQUEST, "Invalid request"),
    VALIDATION_ERROR("COMMERCE-400-VALIDATION", HttpStatus.BAD_REQUEST, "Validation failed"),
    UNAUTHORIZED("COMMERCE-401", HttpStatus.UNAUTHORIZED, "Authentication required"),
    FORBIDDEN("COMMERCE-403", HttpStatus.FORBIDDEN, "Access denied"),
    RESOURCE_NOT_FOUND("COMMERCE-404", HttpStatus.NOT_FOUND, "Resource not found"),
    CART_ITEM_NOT_FOUND("COMMERCE-404-CART-ITEM", HttpStatus.NOT_FOUND, "Cart item not found"),
    ADDRESS_NOT_FOUND("COMMERCE-404-ADDRESS", HttpStatus.NOT_FOUND, "Address not found"),
    PRODUCT_NOT_FOUND("COMMERCE-404-PRODUCT", HttpStatus.NOT_FOUND, "Product not found"),
    ORDER_NOT_FOUND("COMMERCE-404-ORDER", HttpStatus.NOT_FOUND, "Order not found"),
    ORDER_NOT_CANCELLABLE("COMMERCE-409-ORDER-NOT-CANCELLABLE", HttpStatus.CONFLICT, "Order cannot be cancelled"),
    ORDER_NOT_COMPLETABLE("COMMERCE-409-ORDER-NOT-COMPLETABLE", HttpStatus.CONFLICT, "Order cannot be completed yet"),
    ORDER_ITEMS_NOT_DELIVERED("COMMERCE-409-ORDER-ITEMS", HttpStatus.CONFLICT, "No delivered order items to confirm"),
    INVALID_PAYMENT_STATE("COMMERCE-409-PAYMENT-STATE", HttpStatus.CONFLICT, "Payment state does not allow confirmation"),
    INVALID_PAYMENT_METHOD("COMMERCE-400-PAYMENT-METHOD", HttpStatus.BAD_REQUEST, "Invalid payment method"),
    SHOP_VACATION("COMMERCE-409-SHOP-VACATION", HttpStatus.CONFLICT, "Shop is on vacation"),
    INVALID_CART_ITEM("COMMERCE-409-CART-ITEM", HttpStatus.CONFLICT, "Cart item is not available for checkout"),
    NOT_PURCHASABLE("COMMERCE-409-NOT-PURCHASABLE", HttpStatus.CONFLICT, "Product is not purchasable"),
    ACTIVE_PRICE_MISSING("COMMERCE-409-PRICE", HttpStatus.CONFLICT, "Active price is missing"),
    ORDER_SNAPSHOT_INCOMPLETE("COMMERCE-409-ORDER-SNAPSHOT", HttpStatus.CONFLICT, "Order snapshot data is incomplete"),
    SHIPPING_PROFILE_MISSING("COMMERCE-409-SHIPPING-PROFILE", HttpStatus.CONFLICT, "Seller shipping profile is missing"),
    SHIPPING_PROVIDER_UNAVAILABLE("COMMERCE-503-SHIPPING", HttpStatus.SERVICE_UNAVAILABLE, "Shipping provider unavailable"),
    OUT_OF_STOCK("COMMERCE-409-STOCK", HttpStatus.CONFLICT, "Product is out of stock"),
    PRODUCT_REMOVED("COMMERCE-409-PRODUCT-REMOVED", HttpStatus.CONFLICT, "Product has been removed"),
    INVALID_PRODUCT_STATUS("COMMERCE-409-PRODUCT-STATUS", HttpStatus.CONFLICT, "Product status does not allow this action"),
    INVALID_PAGINATION("COMMERCE-400-PAGINATION", HttpStatus.BAD_REQUEST, "Invalid pagination parameters");

    private final String code;
    private final HttpStatus status;
    private final String defaultMessage;

    ErrorCode(String code, HttpStatus status, String defaultMessage) {
        this.code = code;
        this.status = status;
        this.defaultMessage = defaultMessage;
    }

    public String code() {
        return code;
    }

    public HttpStatus status() {
        return status;
    }

    public String defaultMessage() {
        return defaultMessage;
    }
}
