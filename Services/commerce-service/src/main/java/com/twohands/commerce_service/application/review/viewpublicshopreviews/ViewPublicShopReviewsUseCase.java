package com.twohands.commerce_service.application.review.viewpublicshopreviews;

import com.twohands.commerce_service.common.pagination.PageQuery;
import com.twohands.commerce_service.domain.review.ProductReviewSort;
import com.twohands.commerce_service.domain.review.ViewPublicShopReviewsRepository;
import com.twohands.commerce_service.domain.review.ViewPublicShopReviewsResult;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ViewPublicShopReviewsUseCase {

    private static final int DEFAULT_PAGE = 1;
    private static final int DEFAULT_LIMIT = 20;
    private static final int MAX_LIMIT = 50;
    private static final int MIN_RATING = 1;
    private static final int MAX_RATING = 5;

    private final ViewPublicShopReviewsRepository viewPublicShopReviewsRepository;

    public ViewPublicShopReviewsUseCase(ViewPublicShopReviewsRepository viewPublicShopReviewsRepository) {
        this.viewPublicShopReviewsRepository = viewPublicShopReviewsRepository;
    }

    @Transactional(readOnly = true)
    public ViewPublicShopReviewsResult execute(ViewPublicShopReviewsCommand command) {
        PageQuery pageQuery = resolvePageQuery(command.page(), command.limit());
        Integer ratingFilter = resolveRatingFilter(command.rating());
        ProductReviewSort sort = parseSort(command.sort());

        return viewPublicShopReviewsRepository
                .findVisibleShopReviews(command.shopId(), ratingFilter, sort, pageQuery)
                .orElseThrow(() -> new AppException(ErrorCode.SHOP_NOT_FOUND));
    }

    public String successMessage() {
        return "Lay danh sach danh gia shop thanh cong.";
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
