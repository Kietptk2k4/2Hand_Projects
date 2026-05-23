package com.twohands.commerce_service.domain.support;

import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;

public final class WebhookSupportPaginationPolicy {

    public static final int DEFAULT_PAGE = 1;
    public static final int DEFAULT_SIZE = 20;
    public static final int MAX_SIZE = 100;

    private WebhookSupportPaginationPolicy() {
    }

    public static WebhookSupportPageRequest normalize(Integer page, Integer size) {
        int normalizedPage = page == null ? DEFAULT_PAGE : page;
        int normalizedSize = size == null ? DEFAULT_SIZE : size;
        if (normalizedPage < 1) {
            throw paginationError("page", "page must be greater than or equal to 1");
        }
        if (normalizedSize < 1 || normalizedSize > MAX_SIZE) {
            throw paginationError("size", "size must be between 1 and " + MAX_SIZE);
        }
        return new WebhookSupportPageRequest(normalizedPage, normalizedSize);
    }

    private static AppException paginationError(String field, String reason) {
        return new AppException(ErrorCode.VALIDATION_ERROR, ErrorCode.VALIDATION_ERROR.defaultMessage(), field, reason);
    }
}
