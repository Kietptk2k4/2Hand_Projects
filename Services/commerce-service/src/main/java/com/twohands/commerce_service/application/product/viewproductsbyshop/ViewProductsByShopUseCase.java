package com.twohands.commerce_service.application.product.viewproductsbyshop;

import com.twohands.commerce_service.common.pagination.PageQuery;
import com.twohands.commerce_service.domain.discovery.ProductDiscoverySort;
import com.twohands.commerce_service.domain.discovery.ViewProductsByShopRepository;
import com.twohands.commerce_service.domain.discovery.ViewProductsByShopResult;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;

@Service
public class ViewProductsByShopUseCase {

    private static final int DEFAULT_PAGE = 1;
    private static final int DEFAULT_LIMIT = 20;
    private static final int MAX_LIMIT = 50;

    private final ViewProductsByShopRepository viewProductsByShopRepository;
    private final Clock clock;

    public ViewProductsByShopUseCase(ViewProductsByShopRepository viewProductsByShopRepository, Clock clock) {
        this.viewProductsByShopRepository = viewProductsByShopRepository;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public ViewProductsByShopResult execute(ViewProductsByShopCommand command) {
        PageQuery pageQuery = resolvePageQuery(command.page(), command.limit());
        ProductDiscoverySort sort = parseSort(command.sort());

        return viewProductsByShopRepository
                .findVisibleProductsByShopId(command.shopId(), sort, pageQuery, clock.instant())
                .orElseThrow(() -> new AppException(ErrorCode.SHOP_NOT_FOUND));
    }

    public String successMessage() {
        return "Lay danh sach san pham cua shop thanh cong.";
    }

    private PageQuery resolvePageQuery(Integer page, Integer limit) {
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

    private ProductDiscoverySort parseSort(String sort) {
        if (sort == null || sort.isBlank()) {
            return ProductDiscoverySort.NEWEST;
        }
        try {
            return ProductDiscoverySort.valueOf(sort.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new AppException(
                    ErrorCode.VALIDATION_ERROR,
                    "Invalid sort value",
                    "sort",
                    "allowed: NEWEST, PRICE_ASC, PRICE_DESC"
            );
        }
    }
}
