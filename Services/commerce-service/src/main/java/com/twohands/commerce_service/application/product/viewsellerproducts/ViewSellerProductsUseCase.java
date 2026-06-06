package com.twohands.commerce_service.application.product.viewsellerproducts;

import com.twohands.commerce_service.common.pagination.PageMeta;
import com.twohands.commerce_service.common.pagination.PageQuery;
import com.twohands.commerce_service.domain.product.ProductStatus;
import com.twohands.commerce_service.domain.product.SellerProductListItem;
import com.twohands.commerce_service.domain.product.ViewSellerProductCatalogRepository;
import com.twohands.commerce_service.domain.product.ViewSellerProductsResult;
import com.twohands.commerce_service.domain.shop.SellerShopRepository;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Clock;
import java.util.List;
import java.util.Optional;

@Service
public class ViewSellerProductsUseCase {

    private static final int DEFAULT_PAGE = 1;
    private static final int DEFAULT_LIMIT = 10;
    private static final int MAX_LIMIT = 50;

    private final SellerShopRepository sellerShopRepository;
    private final ViewSellerProductCatalogRepository viewSellerProductCatalogRepository;
    private final Clock clock;

    public ViewSellerProductsUseCase(
            SellerShopRepository sellerShopRepository,
            ViewSellerProductCatalogRepository viewSellerProductCatalogRepository,
            Clock clock
    ) {
        this.sellerShopRepository = sellerShopRepository;
        this.viewSellerProductCatalogRepository = viewSellerProductCatalogRepository;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public ViewSellerProductsResult execute(ViewSellerProductsCommand command) {
        sellerShopRepository.findBySellerId(command.sellerId())
                .orElseThrow(() -> new AppException(ErrorCode.SELLER_SHOP_NOT_FOUND));

        PageQuery pageQuery = resolvePageQuery(command.page(), command.limit());
        Optional<ProductStatus> statusFilter = parseStatusFilter(command.status());
        Optional<String> keywordFilter = normalizeKeyword(command.keyword());

        long totalItems = viewSellerProductCatalogRepository.countBySellerId(
                command.sellerId(),
                statusFilter,
                keywordFilter
        );
        List<SellerProductListItem> items = totalItems == 0
                ? List.of()
                : viewSellerProductCatalogRepository.findBySellerId(
                        command.sellerId(),
                        statusFilter,
                        keywordFilter,
                        pageQuery,
                        clock.instant()
                );

        return new ViewSellerProductsResult(
                items,
                PageMeta.of(pageQuery.page(), pageQuery.limit(), totalItems),
                viewSellerProductCatalogRepository.summarizeBySellerId(command.sellerId())
        );
    }

    public String successMessage() {
        return "Lay danh sach san pham thanh cong.";
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

    private Optional<String> normalizeKeyword(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return Optional.empty();
        }
        return Optional.of(keyword.trim());
    }
}
