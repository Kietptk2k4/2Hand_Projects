package com.twohands.commerce_service.application.review.viewproductreviews;

import com.twohands.commerce_service.common.pagination.PageQuery;
import com.twohands.commerce_service.domain.review.ProductReviewSort;
import com.twohands.commerce_service.domain.review.ViewProductReviewsRepository;
import com.twohands.commerce_service.domain.review.ViewProductReviewsResult;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;

@Service
public class ViewProductReviewsUseCase {

    private static final int DEFAULT_PAGE = 1;
    private static final int DEFAULT_LIMIT = 20;
    private static final int MAX_LIMIT = 50;
    private static final int MIN_RATING = 1;
    private static final int MAX_RATING = 5;

    private final ViewProductReviewsRepository viewProductReviewsRepository;
    private final Clock clock;

    public ViewProductReviewsUseCase(ViewProductReviewsRepository viewProductReviewsRepository, Clock clock) {
        this.viewProductReviewsRepository = viewProductReviewsRepository;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public ViewProductReviewsResult execute(ViewProductReviewsCommand command) {
        PageQuery pageQuery = resolvePageQuery(command.page(), command.limit());
        Integer ratingFilter = resolveRatingFilter(command.rating());
        ProductReviewSort sort = parseSort(command.sort());

        return viewProductReviewsRepository
                .findVisibleProductReviews(
                        command.productId(),
                        ratingFilter,
                        sort,
                        pageQuery,
                        clock.instant()
                )
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));
    }

    public String successMessage() {
        return "Lay danh sach danh gia san pham thanh cong.";
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

    private Integer resolveRatingFilter(Integer rating) {
        if (rating == null) {
            return null;
        }
        if (rating < MIN_RATING || rating > MAX_RATING) {
            throw new AppException(
                    ErrorCode.INVALID_RATING,
                    "Rating filter must be between 1 and 5",
                    "rating",
                    "must be between 1 and 5"
            );
        }
        return rating;
    }

    private ProductReviewSort parseSort(String sort) {
        if (sort == null || sort.isBlank()) {
            return ProductReviewSort.NEWEST;
        }
        try {
            return ProductReviewSort.valueOf(sort.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new AppException(
                    ErrorCode.VALIDATION_ERROR,
                    "Invalid sort value",
                    "sort",
                    "allowed: NEWEST, OLDEST, RATING_DESC, RATING_ASC"
            );
        }
    }
}
