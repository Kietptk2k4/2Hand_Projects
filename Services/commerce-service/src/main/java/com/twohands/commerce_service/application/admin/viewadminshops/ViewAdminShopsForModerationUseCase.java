package com.twohands.commerce_service.application.admin.viewadminshops;

import com.twohands.commerce_service.common.pagination.PageMeta;
import com.twohands.commerce_service.common.pagination.PageQuery;
import com.twohands.commerce_service.domain.admin.AdminShopListEntry;
import com.twohands.commerce_service.domain.admin.AdminShopListSort;
import com.twohands.commerce_service.domain.admin.ViewAdminShopsForModerationRepository;
import com.twohands.commerce_service.domain.admin.ViewAdminShopsForModerationResult;
import com.twohands.commerce_service.domain.shop.ShopStatus;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;

@Service
public class ViewAdminShopsForModerationUseCase {

    private static final int DEFAULT_PAGE = 1;
    private static final int DEFAULT_LIMIT = 20;
    private static final int MAX_LIMIT = 50;

    private final ViewAdminShopsForModerationRepository viewAdminShopsForModerationRepository;

    public ViewAdminShopsForModerationUseCase(
            ViewAdminShopsForModerationRepository viewAdminShopsForModerationRepository
    ) {
        this.viewAdminShopsForModerationRepository = viewAdminShopsForModerationRepository;
    }

    @Transactional(readOnly = true)
    public ViewAdminShopsForModerationResult execute(ViewAdminShopsForModerationCommand command) {
        PageQuery pageQuery = resolvePageQuery(command.page(), command.limit());
        Optional<ShopStatus> status = parseStatusFilter(command.status());
        Optional<String> searchQuery = normalizeSearchQuery(command.searchQuery());
        AdminShopListSort sort = parseSort(command.sort());

        long totalItems = viewAdminShopsForModerationRepository.count(status, searchQuery);
        List<AdminShopListEntry> items = totalItems == 0
                ? List.of()
                : viewAdminShopsForModerationRepository.find(status, searchQuery, sort, pageQuery);

        return new ViewAdminShopsForModerationResult(
                items,
                PageMeta.of(pageQuery.page(), pageQuery.limit(), totalItems)
        );
    }

    public String successMessage() {
        return "Lay danh sach shop admin thanh cong.";
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

    private Optional<ShopStatus> parseStatusFilter(String status) {
        if (!StringUtils.hasText(status) || "all".equalsIgnoreCase(status.trim())) {
            return Optional.empty();
        }
        try {
            return Optional.of(ShopStatus.valueOf(status.trim().toUpperCase()));
        } catch (IllegalArgumentException ex) {
            throw new AppException(
                    ErrorCode.VALIDATION_ERROR,
                    "Validation failed",
                    "status",
                    "must be one of ACTIVE, SUSPENDED, CLOSED"
            );
        }
    }

    private AdminShopListSort parseSort(String sort) {
        if (!StringUtils.hasText(sort)) {
            return AdminShopListSort.NEWEST;
        }
        try {
            return AdminShopListSort.valueOf(sort.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new AppException(
                    ErrorCode.VALIDATION_ERROR,
                    "Validation failed",
                    "sort",
                    "must be one of NEWEST, OLDEST, NAME_ASC"
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
}
