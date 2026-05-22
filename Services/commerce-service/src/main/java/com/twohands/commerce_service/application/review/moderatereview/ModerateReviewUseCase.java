package com.twohands.commerce_service.application.review.moderatereview;

import com.twohands.commerce_service.application.review.common.ReviewHiddenOutboxService;
import com.twohands.commerce_service.application.review.common.ReviewRestoredOutboxService;
import com.twohands.commerce_service.domain.outbox.OutboxEvent;
import com.twohands.commerce_service.domain.outbox.OutboxEventRepository;
import com.twohands.commerce_service.domain.review.ModerateReviewRepository;
import com.twohands.commerce_service.domain.review.ModerateReviewResult;
import com.twohands.commerce_service.domain.review.ReviewForModeration;
import com.twohands.commerce_service.domain.review.ReviewModerationAction;
import com.twohands.commerce_service.domain.review.ReviewStatus;
import com.twohands.commerce_service.domain.review.SellerRatingSummary;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Clock;
import java.time.Instant;

@Service
public class ModerateReviewUseCase {

    private final ModerateReviewRepository moderateReviewRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final ReviewHiddenOutboxService reviewHiddenOutboxService;
    private final ReviewRestoredOutboxService reviewRestoredOutboxService;
    private final Clock clock;

    public ModerateReviewUseCase(
            ModerateReviewRepository moderateReviewRepository,
            OutboxEventRepository outboxEventRepository,
            ReviewHiddenOutboxService reviewHiddenOutboxService,
            ReviewRestoredOutboxService reviewRestoredOutboxService,
            Clock clock
    ) {
        this.moderateReviewRepository = moderateReviewRepository;
        this.outboxEventRepository = outboxEventRepository;
        this.reviewHiddenOutboxService = reviewHiddenOutboxService;
        this.reviewRestoredOutboxService = reviewRestoredOutboxService;
        this.clock = clock;
    }

    @Transactional
    public ModerateReviewResult execute(ModerateReviewCommand command) {
        validateReason(command.reason());

        ReviewForModeration review = moderateReviewRepository.findById(command.reviewId())
                .orElseThrow(() -> new AppException(ErrorCode.REVIEW_NOT_FOUND));

        ReviewStatus targetStatus = targetStatus(command.action());
        Instant occurredAt = clock.instant();

        if (review.status() == targetStatus) {
            SellerRatingSummary rating = moderateReviewRepository.recalculateSellerRating(
                    review.sellerId(),
                    occurredAt
            );
            return buildResult(review, review.status(), true, rating, occurredAt);
        }

        boolean updated = moderateReviewRepository.updateStatus(
                review.reviewId(),
                review.status(),
                targetStatus,
                occurredAt
        );
        if (!updated) {
            throw new AppException(ErrorCode.INVALID_REVIEW_MODERATION, "Review status changed concurrently");
        }

        SellerRatingSummary rating = moderateReviewRepository.recalculateSellerRating(
                review.sellerId(),
                occurredAt
        );

        OutboxEvent event = buildModerationEvent(
                command,
                review,
                review.status(),
                targetStatus,
                occurredAt
        );
        outboxEventRepository.save(event);

        return buildResult(review, targetStatus, false, rating, occurredAt);
    }

    public String successMessage(ReviewModerationAction action, boolean alreadyModerated) {
        if (alreadyModerated) {
            return "Review da o trang thai yeu cau.";
        }
        return action == ReviewModerationAction.HIDE
                ? "An review thanh cong."
                : "Khoi phuc review thanh cong.";
    }

    private ReviewStatus targetStatus(ReviewModerationAction action) {
        return switch (action) {
            case HIDE -> ReviewStatus.HIDDEN;
            case RESTORE -> ReviewStatus.VISIBLE;
        };
    }

    private OutboxEvent buildModerationEvent(
            ModerateReviewCommand command,
            ReviewForModeration review,
            ReviewStatus oldStatus,
            ReviewStatus newStatus,
            Instant occurredAt
    ) {
        if (command.action() == ReviewModerationAction.HIDE) {
            return reviewHiddenOutboxService.build(
                    review.reviewId(),
                    review.sellerId(),
                    command.adminId(),
                    oldStatus,
                    newStatus,
                    command.reason(),
                    occurredAt
            );
        }
        return reviewRestoredOutboxService.build(
                review.reviewId(),
                review.sellerId(),
                command.adminId(),
                oldStatus,
                newStatus,
                command.reason(),
                occurredAt
        );
    }

    private ModerateReviewResult buildResult(
            ReviewForModeration review,
            ReviewStatus currentStatus,
            boolean alreadyModerated,
            SellerRatingSummary rating,
            Instant occurredAt
    ) {
        return new ModerateReviewResult(
                review.reviewId(),
                review.orderItemId(),
                review.sellerId(),
                review.buyerId(),
                review.rating(),
                currentStatus,
                review.status(),
                alreadyModerated,
                rating.ratingAvg(),
                rating.ratingCount(),
                occurredAt
        );
    }

    private void validateReason(String reason) {
        if (!StringUtils.hasText(reason)) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "reason is required", "reason", "must not be blank");
        }
    }
}
