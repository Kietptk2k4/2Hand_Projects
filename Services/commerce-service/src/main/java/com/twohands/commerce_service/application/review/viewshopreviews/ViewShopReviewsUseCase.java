package com.twohands.commerce_service.application.review.viewshopreviews;

import com.twohands.commerce_service.common.pagination.PageQuery;
import com.twohands.commerce_service.domain.review.ProductReviewSort;
import com.twohands.commerce_service.domain.review.ReviewStatus;
import com.twohands.commerce_service.domain.review.ViewShopReviewsRepository;
import com.twohands.commerce_service.domain.review.ViewShopReviewsResult;
import com.twohands.commerce_service.domain.shop.SellerShop;
import com.twohands.commerce_service.domain.shop.SellerShopRepository;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class ViewShopReviewsUseCase {

    private static final int DEFAULT_PAGE = 1;
    private static final int DEFAULT_LIMIT = 20;
    private static final int MAX_LIMIT = 50;
    private static final int MIN_RATING = 1;
    private static final int MAX_RATING = 5;

    private final SellerShopRepository sellerShopRepository;
    private final ViewShopReviewsRepository viewShopReviewsRepository;

    public ViewShopReviewsUseCase(
            SellerShopRepository sellerShopRepository,
            ViewShopReviewsRepository viewShopReviewsRepository
    ) {
        this.sellerShopRepository = sellerShopRepository;
        this.viewShopReviewsRepository = viewShopReviewsRepository;
    }

    @Transactional(readOnly = true)
    public ViewShopReviewsResult execute(ViewShopReviewsCommand command) {
        SellerShop shop = sellerShopRepository.findBySellerId(command.sellerId())
                .orElseThrow(() -> new AppException(ErrorCode.SELLER_SHOP_NOT_FOUND));

        PageQuery pageQuery = resolvePageQuery(command.page(), command.limit());
        Integer ratingFilter = resolveRatingFilter(command.rating());
        ReviewStatus statusFilter = resolveStatusFilter(command.status());

        return viewShopReviewsRepository.findShopReviews(
                command.sellerId(),
                shop.id(),
                ratingFilter,
                statusFilter,
                ProductReviewSort.NEWEST,
                pageQuery
        );
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

    private ReviewStatus resolveStatusFilter(String status) {
        if (!StringUtils.hasText(status)) {
            return ReviewStatus.VISIBLE;
        }
        try {
            return ReviewStatus.valueOf(status.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new AppException(
                    ErrorCode.VALIDATION_ERROR,
                    "Validation failed",
                    "status",
                    "must be one of VISIBLE, HIDDEN"
            );
        }
    }
}
