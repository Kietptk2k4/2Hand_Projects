package com.twohands.commerce_service.application.finance.payout;

import com.twohands.commerce_service.common.pagination.PageQuery;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;

public final class SellerPayoutPageSupport {

    private static final int DEFAULT_PAGE = 1;
    private static final int DEFAULT_LIMIT = 20;
    private static final int MAX_LIMIT = 50;

    private SellerPayoutPageSupport() {
    }

    public static PageQuery resolvePageQuery(Integer page, Integer limit) {
        int resolvedPage = page == null ? DEFAULT_PAGE : page;
        int resolvedLimit = limit == null ? DEFAULT_LIMIT : limit;
        if (resolvedPage < 1) {
            throw new AppException(ErrorCode.INVALID_PAGINATION, "page must be >= 1", "page", "must be >= 1");
        }
        if (resolvedLimit < 1 || resolvedLimit > MAX_LIMIT) {
            throw new AppException(
                    ErrorCode.INVALID_PAGINATION,
                    "limit must be between 1 and " + MAX_LIMIT,
                    "limit",
                    "must be between 1 and " + MAX_LIMIT
            );
        }
        return new PageQuery(resolvedPage, resolvedLimit);
    }
}
