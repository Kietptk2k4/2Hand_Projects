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
    INVALID_PHONE("COMMERCE-400-PHONE", HttpStatus.BAD_REQUEST, "Invalid phone number"),
    ADDRESS_DEFAULT_CONFLICT("COMMERCE-409-ADDRESS-DEFAULT", HttpStatus.CONFLICT, "Default address conflict"),
    PRODUCT_NOT_FOUND("COMMERCE-404-PRODUCT", HttpStatus.NOT_FOUND, "Product not found"),
    CATEGORY_NOT_FOUND("COMMERCE-404-CATEGORY", HttpStatus.NOT_FOUND, "Product category not found"),
    BRAND_NOT_FOUND("COMMERCE-404-BRAND", HttpStatus.NOT_FOUND, "Product brand not found"),
    SELLER_SHOP_NOT_FOUND("COMMERCE-409-SELLER-SHOP", HttpStatus.CONFLICT, "Seller does not have a shop"),
    SHOP_ALREADY_EXISTS("COMMERCE-409-SHOP-EXISTS", HttpStatus.CONFLICT, "Seller already has a shop"),
    INVALID_MEDIA_URL("COMMERCE-400-MEDIA-URL", HttpStatus.BAD_REQUEST, "Media URL is invalid"),
    INVALID_MEDIA_TYPE("COMMERCE-400-MEDIA-TYPE", HttpStatus.BAD_REQUEST, "Media type is not allowed"),
    INVALID_MEDIA_SIZE("COMMERCE-400-MEDIA-SIZE", HttpStatus.BAD_REQUEST, "Media file size exceeds limit"),
    REVIEW_MEDIA_LIMIT_EXCEEDED("COMMERCE-409-REVIEW-MEDIA", HttpStatus.CONFLICT, "Review media count limit exceeded"),
    OBJECT_STORAGE_UNAVAILABLE("COMMERCE-503-MINIO", HttpStatus.SERVICE_UNAVAILABLE, "Object storage is unavailable"),
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
    VNPAY_PROVIDER_UNAVAILABLE("COMMERCE-503-VNPAY", HttpStatus.SERVICE_UNAVAILABLE, "VNPay provider unavailable"),
    SHOP_VACATION("COMMERCE-409-SHOP-VACATION", HttpStatus.CONFLICT, "Shop is on vacation"),
    INVALID_CART_ITEM("COMMERCE-409-CART-ITEM", HttpStatus.CONFLICT, "Cart item is not available for checkout"),
    NOT_PURCHASABLE("COMMERCE-409-NOT-PURCHASABLE", HttpStatus.CONFLICT, "Product is not purchasable"),
    ACTIVE_PRICE_MISSING("COMMERCE-409-PRICE", HttpStatus.CONFLICT, "Active price is missing"),
    PRICE_WINDOW_OVERLAP("COMMERCE-409-PRICE-WINDOW", HttpStatus.CONFLICT, "Product price window overlaps an existing price"),
    ORDER_SNAPSHOT_INCOMPLETE("COMMERCE-409-ORDER-SNAPSHOT", HttpStatus.CONFLICT, "Order snapshot data is incomplete"),
    SHIPPING_PROFILE_MISSING("COMMERCE-409-SHIPPING-PROFILE", HttpStatus.CONFLICT, "Seller shipping profile is missing"),
    SHIPPING_PROVIDER_UNAVAILABLE("COMMERCE-503-SHIPPING", HttpStatus.SERVICE_UNAVAILABLE, "Shipping provider unavailable"),
    OUT_OF_STOCK("COMMERCE-409-STOCK", HttpStatus.CONFLICT, "Product is out of stock"),
    INVENTORY_NOT_FOUND("COMMERCE-409-INVENTORY", HttpStatus.CONFLICT, "Product inventory record not found"),
    PRODUCT_REMOVED("COMMERCE-409-PRODUCT-REMOVED", HttpStatus.CONFLICT, "Product has been removed"),
    INVALID_PRODUCT_STATUS("COMMERCE-409-PRODUCT-STATUS", HttpStatus.CONFLICT, "Product status does not allow this action"),
    INVALID_PAGINATION("COMMERCE-400-PAGINATION", HttpStatus.BAD_REQUEST, "Invalid pagination parameters"),
    INVALID_SEARCH_KEYWORD("COMMERCE-400-SEARCH-KEYWORD", HttpStatus.BAD_REQUEST, "Invalid search keyword"),
    INVALID_RATING("COMMERCE-400-RATING", HttpStatus.BAD_REQUEST, "Invalid rating"),
    ORDER_ITEM_NOT_REVIEWABLE("COMMERCE-409-ORDER-ITEM-REVIEW", HttpStatus.CONFLICT, "Order item is not reviewable"),
    REVIEW_ALREADY_EXISTS("COMMERCE-409-REVIEW-EXISTS", HttpStatus.CONFLICT, "Review already exists for this order item"),
    ORDER_NOT_PROCESSING("COMMERCE-409-ORDER-PROCESSING", HttpStatus.CONFLICT, "Order is not in PROCESSING status"),
    ORDER_ITEM_ALREADY_SHIPPED("COMMERCE-409-ORDER-ITEM-SHIPPED", HttpStatus.CONFLICT, "Order item already has a shipment"),
    ORDER_ITEM_NOT_PROCESSABLE("COMMERCE-409-ORDER-ITEM-PROCESS", HttpStatus.CONFLICT, "Order item cannot be processed"),
    ORDER_ITEM_NOT_OWNED("COMMERCE-403-ORDER-ITEM", HttpStatus.FORBIDDEN, "Order item does not belong to seller"),
    BUYER_ADDRESS_NOT_FOUND("COMMERCE-404-BUYER-ADDRESS", HttpStatus.NOT_FOUND, "Buyer delivery address not found"),
    INVALID_SHIPMENT_CARRIER("COMMERCE-400-SHIPMENT-CARRIER", HttpStatus.BAD_REQUEST, "Invalid shipment carrier"),
    INVALID_SHIPMENT_TYPE("COMMERCE-400-SHIPMENT-TYPE", HttpStatus.BAD_REQUEST, "Invalid shipment type"),
    GHN_PROVIDER_UNAVAILABLE("COMMERCE-503-GHN", HttpStatus.SERVICE_UNAVAILABLE, "GHN provider unavailable"),
    GHN_ADDRESS_NOT_READY("COMMERCE-400-GHN-ADDRESS", HttpStatus.BAD_REQUEST, "Address is not ready for GHN"),
    SHIPMENT_NOT_FOUND("COMMERCE-404-SHIPMENT", HttpStatus.NOT_FOUND, "Shipment not found"),
    INVALID_SHIPMENT_STATUS("COMMERCE-409-SHIPMENT-STATUS", HttpStatus.CONFLICT, "Invalid shipment status"),
    SHIPMENT_CARRIER_NOT_EDITABLE("COMMERCE-409-SHIPMENT-CARRIER", HttpStatus.CONFLICT, "Shipment carrier cannot be updated by seller"),
    DUPLICATE_TRACKING_NUMBER("COMMERCE-409-TRACKING", HttpStatus.CONFLICT, "Tracking number already exists"),
    REVIEW_NOT_FOUND("COMMERCE-404-REVIEW", HttpStatus.NOT_FOUND, "Review not found"),
    REVIEW_NOT_VISIBLE("COMMERCE-409-REVIEW-VISIBLE", HttpStatus.CONFLICT, "Review is not visible"),
    REVIEW_REPLY_EXISTS("COMMERCE-409-REVIEW-REPLY", HttpStatus.CONFLICT, "Review reply already exists"),
    INVALID_REVIEW_MODERATION("COMMERCE-400-REVIEW-MODERATION", HttpStatus.BAD_REQUEST, "Invalid review moderation action"),
    SHOP_NOT_FOUND("COMMERCE-404-SHOP", HttpStatus.NOT_FOUND, "Shop not found"),
    INVALID_SHOP_MODERATION("COMMERCE-400-SHOP-MODERATION", HttpStatus.BAD_REQUEST, "Invalid shop moderation action"),
    INVALID_SHOP_STATUS("COMMERCE-409-SHOP-STATUS", HttpStatus.CONFLICT, "Invalid shop status transition"),
    PAYOUT_ACCOUNT_NOT_FOUND("COMMERCE-404-PAYOUT-ACCOUNT", HttpStatus.NOT_FOUND, "Payout account not found"),
    PAYOUT_REQUEST_NOT_FOUND("COMMERCE-404-PAYOUT-REQUEST", HttpStatus.NOT_FOUND, "Payout request not found"),
    INSUFFICIENT_PAYOUT_BALANCE("COMMERCE-409-PAYOUT-BALANCE", HttpStatus.CONFLICT, "Insufficient available balance for payout"),
    PAYOUT_AMOUNT_BELOW_MINIMUM("COMMERCE-400-PAYOUT-MIN", HttpStatus.BAD_REQUEST, "Payout amount is below minimum"),
    REFUND_ALREADY_REQUESTED("COMMERCE-409-REFUND-REQUESTED", HttpStatus.CONFLICT, "Refund request already exists for this order"),
    REFUND_REQUEST_NOT_FOUND("COMMERCE-404-REFUND-REQUEST", HttpStatus.NOT_FOUND, "Refund request not found"),
    INVALID_REFUND_REQUEST_STATE("COMMERCE-409-REFUND-STATE", HttpStatus.CONFLICT, "Refund request status does not allow this action"),
    INVALID_PAYOUT_REQUEST_STATE("COMMERCE-409-PAYOUT-STATE", HttpStatus.CONFLICT, "Payout request status does not allow this action");

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
