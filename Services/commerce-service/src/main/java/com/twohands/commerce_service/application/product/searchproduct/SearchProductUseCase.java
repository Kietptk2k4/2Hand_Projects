package com.twohands.commerce_service.application.product.searchproduct;

import com.twohands.commerce_service.common.pagination.PageMeta;
import com.twohands.commerce_service.common.pagination.PageQuery;
import com.twohands.commerce_service.domain.discovery.ProductCardSummary;
import com.twohands.commerce_service.domain.discovery.ProductDiscoveryRepository;
import com.twohands.commerce_service.domain.discovery.ProductDiscoverySort;
import com.twohands.commerce_service.domain.discovery.SearchProductResult;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.regex.Pattern;

@Service
public class SearchProductUseCase {

    private static final int DEFAULT_PAGE = 1;
    private static final int DEFAULT_LIMIT = 20;
    private static final int MAX_LIMIT = 50;
    private static final int MIN_KEYWORD_LENGTH = 2;
    private static final int MAX_KEYWORD_LENGTH = 255;
    private static final Pattern WHITESPACE = Pattern.compile("\\s+");

    private final ProductDiscoveryRepository productDiscoveryRepository;
    private final Clock clock;

    public SearchProductUseCase(ProductDiscoveryRepository productDiscoveryRepository, Clock clock) {
        this.productDiscoveryRepository = productDiscoveryRepository;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public SearchProductResult execute(SearchProductCommand command) {
        String keyword = normalizeKeyword(command.keyword());
        PageQuery pageQuery = resolvePageQuery(command.page(), command.limit());
        ProductDiscoverySort sort = parseSort(command.sort());
        String likePattern = toLikePattern(keyword);

        Instant now = clock.instant();
        long totalItems = productDiscoveryRepository.countVisibleProductsByKeyword(likePattern, now);
        List<ProductCardSummary> items = totalItems == 0
                ? List.of()
                : productDiscoveryRepository.findVisibleProductsByKeyword(likePattern, sort, pageQuery, now);

        return new SearchProductResult(keyword, items, PageMeta.of(pageQuery.page(), pageQuery.limit(), totalItems));
    }

    public String successMessage() {
        return "Tim kiem san pham thanh cong.";
    }

    private String normalizeKeyword(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            throw new AppException(
                    ErrorCode.INVALID_SEARCH_KEYWORD,
                    "Search keyword is required",
                    "q",
                    "must not be empty"
            );
        }
        String trimmed = WHITESPACE.matcher(keyword.trim()).replaceAll(" ");
        if (trimmed.length() < MIN_KEYWORD_LENGTH) {
            throw new AppException(
                    ErrorCode.INVALID_SEARCH_KEYWORD,
                    "Search keyword is too short",
                    "q",
                    "minimum length is " + MIN_KEYWORD_LENGTH
            );
        }
        if (trimmed.length() > MAX_KEYWORD_LENGTH) {
            throw new AppException(
                    ErrorCode.INVALID_SEARCH_KEYWORD,
                    "Search keyword is too long",
                    "q",
                    "maximum length is " + MAX_KEYWORD_LENGTH
            );
        }
        return trimmed;
    }

    private String toLikePattern(String keyword) {
        String escaped = keyword
                .replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
        return "%" + escaped + "%";
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
