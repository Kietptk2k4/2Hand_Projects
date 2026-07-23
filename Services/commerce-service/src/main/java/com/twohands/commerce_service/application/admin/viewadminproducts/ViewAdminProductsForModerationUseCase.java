package com.twohands.commerce_service.application.admin.viewadminproducts;

import com.twohands.commerce_service.common.pagination.PageMeta;
import com.twohands.commerce_service.common.pagination.PageQuery;
import com.twohands.commerce_service.domain.admin.AdminProductListEntry;
import com.twohands.commerce_service.domain.admin.AdminProductListSort;
import com.twohands.commerce_service.domain.admin.ViewAdminProductsForModerationRepository;
import com.twohands.commerce_service.domain.admin.ViewAdminProductsForModerationResult;
import com.twohands.commerce_service.domain.product.ProductStatus;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;

@Service
public class ViewAdminProductsForModerationUseCase {

    private static final int DEFAULT_PAGE = 1;
    private static final int DEFAULT_LIMIT = 20;
    private static final int MAX_LIMIT = 100;

    private final ViewAdminProductsForModerationRepository viewAdminProductsForModerationRepository;

    public ViewAdminProductsForModerationUseCase(
            ViewAdminProductsForModerationRepository viewAdminProductsForModerationRepository
    ) {
        this.viewAdminProductsForModerationRepository = viewAdminProductsForModerationRepository;
    }

    @Transactional(readOnly = true)
    public ViewAdminProductsForModerationResult execute(ViewAdminProductsForModerationCommand command) {
        PageQuery pageQuery = resolvePageQuery(command.page(), command.limit());
        Optional<ProductStatus> status = parseStatusFilter(command.status());
        Optional<String> searchQuery = normalizeSearchQuery(command.searchQuery());
        AdminProductListSort sort = parseSort(command.sort());

        long totalItems = viewAdminProductsForModerationRepository.count(status, searchQuery);
        List<AdminProductListEntry> items = totalItems == 0
                ? List.of()
                : viewAdminProductsForModerationRepository.find(status, searchQuery, sort, pageQuery);

        return new ViewAdminProductsForModerationResult(
                items,
                PageMeta.of(pageQuery.page(), pageQuery.limit(), totalItems)
        );
    }

    public String successMessage() {
        return "Lay danh sach san pham admin thanh cong.";
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

    private Optional<ProductStatus> parseStatusFilter(String status) {
        if (!StringUtils.hasText(status)) {
            return Optional.empty();
        }
        try {
            return Optional.of(ProductStatus.valueOf(status.trim().toUpperCase()));
        } catch (IllegalArgumentException ex) {
            throw new AppException(
                    ErrorCode.VALIDATION_ERROR,
                    "Validation failed",
                    "status",
                    "must be one of DRAFT, ACTIVE, OUT_OF_STOCK, PAUSED, ARCHIVED, REMOVED"
            );
        }
    }

    private Optional<String> normalizeSearchQuery(String searchQuery) {
        if (!StringUtils.hasText(searchQuery)) {
            return Optional.empty();
        }
        String trimmed = searchQuery.trim();
        return trimmed.isEmpty() ? Optional.empty() : Optional.of(trimmed);
    }

    private AdminProductListSort parseSort(String sort) {
        if (!StringUtils.hasText(sort)) {
            return AdminProductListSort.NEWEST;
        }
        try {
            return AdminProductListSort.valueOf(sort.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new AppException(
                    ErrorCode.VALIDATION_ERROR,
                    "Validation failed",
                    "sort",
                    "must be one of NEWEST, OLDEST, PRICE_ASC, PRICE_DESC, UPDATED_AT"
            );
        }
    }
}
