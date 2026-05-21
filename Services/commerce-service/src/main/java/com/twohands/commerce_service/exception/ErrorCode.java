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
    CATEGORY_NOT_FOUND("COMMERCE-404-CATEGORY", HttpStatus.NOT_FOUND, "Product category not found"),
    SELLER_SHOP_NOT_FOUND("COMMERCE-409-SELLER-SHOP", HttpStatus.CONFLICT, "Seller does not have a shop"),
    SHOP_NOT_OPERATING("COMMERCE-409-SHOP-STATUS", HttpStatus.CONFLICT, "Shop is not available for this action"),
    ORDER_NOT_FOUND("COMMERCE-404-ORDER", HttpStatus.NOT_FOUND, "Order not found"),
    ORDER_ITEM_NOT_FOUND("COMMERCE-404-ORDER-ITEM", HttpStatus.NOT_FOUND, "Order item not found"),
    PAYMENT_NOT_FOUND("COMMERCE-404-PAYMENT", HttpStatus.NOT_FOUND, "Payment not found"),
    ORDER_NOT_CANCELLABLE("COMMERCE-409-ORDER-NOT-CANCELLABLE", HttpStatus.CONFLICT, "Order cannot be cancelled"),
    ORDER_NOT_COMPLETABLE("COMMERCE-409-ORDER-NOT-COMPLETABLE", HttpStatus.CONFLICT, "Order cannot be completed yet"),
    ORDER_ITEMS_NOT_DELIVERED("COMMERCE-409-ORDER-ITEMS", HttpStatus.CONFLICT, "No delivered order items to confirm"),
    INVALID_PAYMENT_STATE("COMMERCE-409-PAYMENT-STATE", HttpStatus.CONFLICT, "Payment state does not allow confirmation"),
    INVALID_PAYMENT_METHOD("COMMERCE-400-PAYMENT-METHOD", HttpStatus.BAD_REQUEST, "Invalid payment method"),
    INVALID_PAYMENT_AMOUNT("COMMERCE-400-PAYMENT-AMOUNT", HttpStatus.BAD_REQUEST, "Invalid payment amount"),
    PAYMENT_ALREADY_EXISTS("COMMERCE-409-PAYMENT-EXISTS", HttpStatus.CONFLICT, "Payment already exists for this order"),
    ORDER_NOT_AWAITING_PAYMENT("COMMERCE-409-ORDER-AWAITING-PAYMENT", HttpStatus.CONFLICT, "Order is not awaiting payment"),
    PAYOS_PROVIDER_UNAVAILABLE("COMMERCE-503-PAYOS", HttpStatus.SERVICE_UNAVAILABLE, "PayOS provider unavailable"),
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
    INVALID_PAGINATION("COMMERCE-400-PAGINATION", HttpStatus.BAD_REQUEST, "Invalid pagination parameters"),
    INVALID_RATING("COMMERCE-400-RATING", HttpStatus.BAD_REQUEST, "Invalid rating"),
    ORDER_ITEM_NOT_REVIEWABLE("COMMERCE-409-ORDER-ITEM-REVIEW", HttpStatus.CONFLICT, "Order item is not reviewable"),
    REVIEW_ALREADY_EXISTS("COMMERCE-409-REVIEW-EXISTS", HttpStatus.CONFLICT, "Review already exists for this order item"),
    ORDER_NOT_PROCESSING("COMMERCE-409-ORDER-PROCESSING", HttpStatus.CONFLICT, "Order is not in PROCESSING status"),
    ORDER_ITEM_ALREADY_SHIPPED("COMMERCE-409-ORDER-ITEM-SHIPPED", HttpStatus.CONFLICT, "Order item already has a shipment"),
    ORDER_ITEM_NOT_OWNED("COMMERCE-403-ORDER-ITEM", HttpStatus.FORBIDDEN, "Order item does not belong to seller"),
    BUYER_ADDRESS_NOT_FOUND("COMMERCE-404-BUYER-ADDRESS", HttpStatus.NOT_FOUND, "Buyer delivery address not found"),
    INVALID_SHIPMENT_CARRIER("COMMERCE-400-SHIPMENT-CARRIER", HttpStatus.BAD_REQUEST, "Invalid shipment carrier"),
    INVALID_SHIPMENT_TYPE("COMMERCE-400-SHIPMENT-TYPE", HttpStatus.BAD_REQUEST, "Invalid shipment type"),
    GHN_PROVIDER_UNAVAILABLE("COMMERCE-503-GHN", HttpStatus.SERVICE_UNAVAILABLE, "GHN provider unavailable");

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
