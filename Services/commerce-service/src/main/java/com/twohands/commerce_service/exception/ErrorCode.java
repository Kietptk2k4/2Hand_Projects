package com.twohands.commerce_service.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
    INTERNAL_ERROR("COMMERCE-500", HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error"),
    BAD_REQUEST("COMMERCE-400", HttpStatus.BAD_REQUEST, "Invalid request"),
    VALIDATION_ERROR("COMMERCE-400-VALIDATION", HttpStatus.BAD_REQUEST, "Validation failed"),
    UNAUTHORIZED("COMMERCE-401", HttpStatus.UNAUTHORIZED, "Authentication required"),
    FORBIDDEN("COMMERCE-403", HttpStatus.FORBIDDEN, "Access denied"),
    RESOURCE_NOT_FOUND("COMMERCE-404", HttpStatus.NOT_FOUND, "Resource not found"),
    PRODUCT_NOT_FOUND("COMMERCE-404-PRODUCT", HttpStatus.NOT_FOUND, "Product not found"),
    NOT_PURCHASABLE("COMMERCE-409-NOT-PURCHASABLE", HttpStatus.CONFLICT, "Product is not purchasable"),
    ACTIVE_PRICE_MISSING("COMMERCE-409-PRICE", HttpStatus.CONFLICT, "Active price is missing"),
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
