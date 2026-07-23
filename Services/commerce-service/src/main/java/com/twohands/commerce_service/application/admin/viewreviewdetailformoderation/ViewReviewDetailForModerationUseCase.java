package com.twohands.commerce_service.application.admin.viewreviewdetailformoderation;

import com.twohands.commerce_service.domain.admin.AdminReviewDetailEntry;
import com.twohands.commerce_service.domain.admin.ViewReviewDetailForModerationRepository;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ViewReviewDetailForModerationUseCase {

    private static final String SUCCESS_MESSAGE = "Lay chi tiet review kiem duyet thanh cong.";

    private final ViewReviewDetailForModerationRepository viewReviewDetailForModerationRepository;

    public ViewReviewDetailForModerationUseCase(
            ViewReviewDetailForModerationRepository viewReviewDetailForModerationRepository
    ) {
        this.viewReviewDetailForModerationRepository = viewReviewDetailForModerationRepository;
    }

    @Transactional(readOnly = true)
    public ViewReviewDetailForModerationResult execute(ViewReviewDetailForModerationCommand command) {
        AdminReviewDetailEntry entry = viewReviewDetailForModerationRepository
                .findByReviewId(command.reviewId())
                .orElseThrow(() -> new AppException(ErrorCode.REVIEW_NOT_FOUND));

        return new ViewReviewDetailForModerationResult(
                entry.reviewId(),
                entry.orderItemId(),
                entry.productId(),
                entry.productTitle(),
                entry.productThumbnailUrl(),
                entry.buyerId(),
                entry.sellerId(),
                entry.shopId(),
                entry.shopName(),
                entry.rating(),
                entry.comment(),
                entry.status(),
                entry.createdAt(),
                entry.updatedAt()
        );
    }

    public String successMessage() {
        return SUCCESS_MESSAGE;
    }
}
