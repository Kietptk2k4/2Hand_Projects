package com.twohands.commerce_service.application.product.filterproductsbycategory;

import com.twohands.commerce_service.common.pagination.PageMeta;
import com.twohands.commerce_service.common.pagination.PageQuery;
import com.twohands.commerce_service.domain.catalog.ActiveCategory;
import com.twohands.commerce_service.domain.catalog.CategoryReadRepository;
import com.twohands.commerce_service.domain.discovery.FilterProductsByCategoryResult;
import com.twohands.commerce_service.domain.discovery.ProductCardSummary;
import com.twohands.commerce_service.domain.discovery.ProductDiscoveryRepository;
import com.twohands.commerce_service.domain.discovery.ProductDiscoverySort;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class FilterProductsByCategoryUseCase {

    private static final int DEFAULT_PAGE = 1;
    private static final int DEFAULT_LIMIT = 20;
    private static final int MAX_LIMIT = 50;

    private final CategoryReadRepository categoryReadRepository;
    private final ProductDiscoveryRepository productDiscoveryRepository;
    private final Clock clock;

    public FilterProductsByCategoryUseCase(
            CategoryReadRepository categoryReadRepository,
            ProductDiscoveryRepository productDiscoveryRepository,
            Clock clock
    ) {
        this.categoryReadRepository = categoryReadRepository;
        this.productDiscoveryRepository = productDiscoveryRepository;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public FilterProductsByCategoryResult execute(FilterProductsByCategoryCommand command) {
        PageQuery pageQuery = resolvePageQuery(command.page(), command.limit());
        ProductDiscoverySort sort = parseSort(command.sort());
        boolean includeChildren = command.includeChildren() == null || command.includeChildren();

        ActiveCategory category = categoryReadRepository.findById(command.categoryId())
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));

        List<UUID> categoryIds = categoryReadRepository.resolveCategoryIdsForFilter(
                category.id(),
                category.path(),
                includeChildren
        );

        Instant now = clock.instant();
        long totalItems = productDiscoveryRepository.countVisibleProductsByCategories(categoryIds, now);
        List<ProductCardSummary> items = totalItems == 0
                ? List.of()
                : productDiscoveryRepository.findVisibleProductsByCategories(
                        categoryIds,
                        sort,
                        pageQuery,
                        now
                );

        return new FilterProductsByCategoryResult(
                category,
                includeChildren,
                items,
                PageMeta.of(pageQuery.page(), pageQuery.limit(), totalItems)
        );
    }

    public String successMessage() {
        return "Lay danh sach san pham theo category thanh cong.";
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
