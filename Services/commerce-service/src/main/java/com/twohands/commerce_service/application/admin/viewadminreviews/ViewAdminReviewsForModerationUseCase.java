package com.twohands.commerce_service.application.admin.viewadminreviews;

import com.twohands.commerce_service.common.pagination.PageMeta;
import com.twohands.commerce_service.common.pagination.PageQuery;
import com.twohands.commerce_service.domain.admin.AdminReviewListEntry;
import com.twohands.commerce_service.domain.admin.ViewAdminReviewsForModerationRepository;
import com.twohands.commerce_service.domain.admin.ViewAdminReviewsForModerationResult;
import com.twohands.commerce_service.domain.review.ReviewStatus;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;

@Service
public class ViewAdminReviewsForModerationUseCase {

    private static final int DEFAULT_PAGE = 1;
    private static final int DEFAULT_LIMIT = 20;
    private static final int MAX_LIMIT = 50;
    private static final int MIN_RATING = 1;
    private static final int MAX_RATING = 5;

    private final ViewAdminReviewsForModerationRepository viewAdminReviewsForModerationRepository;

    public ViewAdminReviewsForModerationUseCase(
            ViewAdminReviewsForModerationRepository viewAdminReviewsForModerationRepository
    ) {
        this.viewAdminReviewsForModerationRepository = viewAdminReviewsForModerationRepository;
    }

    @Transactional(readOnly = true)
    public ViewAdminReviewsForModerationResult execute(ViewAdminReviewsForModerationCommand command) {
        PageQuery pageQuery = resolvePageQuery(command.page(), command.limit());
        Optional<ReviewStatus> status = parseStatusFilter(command.status());
        Optional<Integer> rating = resolveRatingFilter(command.rating());
        Optional<String> searchQuery = normalizeSearchQuery(command.searchQuery());

        long totalItems = viewAdminReviewsForModerationRepository.count(status, rating, searchQuery);
        List<AdminReviewListEntry> items = totalItems == 0
                ? List.of()
                : viewAdminReviewsForModerationRepository.find(status, rating, searchQuery, pageQuery);

        return new ViewAdminReviewsForModerationResult(
                items,
                PageMeta.of(pageQuery.page(), pageQuery.limit(), totalItems)
        );
    }

    public String successMessage() {
        return "Lay danh sach review admin thanh cong.";
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

    private Optional<ReviewStatus> parseStatusFilter(String status) {
        if (!StringUtils.hasText(status)) {
            return Optional.empty();
        }
        try {
            return Optional.of(ReviewStatus.valueOf(status.trim().toUpperCase()));
        } catch (IllegalArgumentException ex) {
            throw new AppException(
                    ErrorCode.VALIDATION_ERROR,
                    "Validation failed",
                    "status",
                    "must be one of VISIBLE, HIDDEN"
            );
        }
    }

    private Optional<Integer> resolveRatingFilter(Integer rating) {
        if (rating == null) {
            return Optional.empty();
        }
        if (rating < MIN_RATING || rating > MAX_RATING) {
            throw new AppException(
                    ErrorCode.INVALID_RATING,
                    "Rating filter must be between 1 and 5",
                    "rating",
                    "must be between 1 and 5"
            );
        }
        return Optional.of(rating);
    }

    private Optional<String> normalizeSearchQuery(String searchQuery) {
        if (!StringUtils.hasText(searchQuery)) {
            return Optional.empty();
        }
        String trimmed = searchQuery.trim();
        return trimmed.isEmpty() ? Optional.empty() : Optional.of(trimmed);
    }
}
