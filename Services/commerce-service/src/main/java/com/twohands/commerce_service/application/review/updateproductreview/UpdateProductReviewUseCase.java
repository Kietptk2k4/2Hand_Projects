package com.twohands.commerce_service.application.review.updateproductreview;

import com.twohands.commerce_service.application.review.common.ReviewUpdatedOutboxService;
import com.twohands.commerce_service.domain.outbox.OutboxEventRepository;
import com.twohands.commerce_service.domain.review.ReviewStatus;
import com.twohands.commerce_service.domain.review.SellerRatingSummary;
import com.twohands.commerce_service.domain.review.UpdateProductReviewDraft;
import com.twohands.commerce_service.domain.review.UpdateProductReviewRepository;
import com.twohands.commerce_service.domain.review.UpdateProductReviewResult;
import com.twohands.commerce_service.domain.review.UpdateProductReviewSnapshot;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Clock;
import java.time.Instant;

@Service
public class UpdateProductReviewUseCase {

    private static final int MIN_RATING = 1;
    private static final int MAX_RATING = 5;

    private final UpdateProductReviewRepository updateProductReviewRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final ReviewUpdatedOutboxService reviewUpdatedOutboxService;
    private final Clock clock;

    public UpdateProductReviewUseCase(
            UpdateProductReviewRepository updateProductReviewRepository,
            OutboxEventRepository outboxEventRepository,
            ReviewUpdatedOutboxService reviewUpdatedOutboxService,
            Clock clock
    ) {
        this.updateProductReviewRepository = updateProductReviewRepository;
        this.outboxEventRepository = outboxEventRepository;
        this.reviewUpdatedOutboxService = reviewUpdatedOutboxService;
        this.clock = clock;
    }

    @Transactional
    public UpdateProductReviewResult execute(UpdateProductReviewCommand command) {
        validateHasUpdates(command);

        UpdateProductReviewSnapshot existing = updateProductReviewRepository
                .findByIdAndBuyerId(command.reviewId(), command.buyerId())
                .orElseThrow(() -> new AppException(ErrorCode.REVIEW_NOT_FOUND));

        if (existing.status() == ReviewStatus.HIDDEN) {
            throw new AppException(ErrorCode.REVIEW_NOT_VISIBLE, "Hidden review cannot be updated");
        }

        if (command.rating() != null) {
            validateRating(command.rating());
        }

        int newRating = command.rating() != null ? command.rating() : existing.rating();
        String newComment = resolveComment(command.comment(), existing.comment());
        boolean ratingChanged = newRating != existing.rating();

        Instant now = clock.instant();
        UpdateProductReviewResult updated = updateProductReviewRepository.updateReview(
                new UpdateProductReviewDraft(
                        existing.reviewId(),
                        existing.buyerId(),
                        newRating,
                        newComment
                ),
                now
        );

        SellerRatingSummary ratingSummary = ratingChanged
                ? updateProductReviewRepository.recalculateSellerRating(existing.sellerId(), now)
                : updateProductReviewRepository.loadSellerRatingSummary(existing.sellerId());

        outboxEventRepository.save(reviewUpdatedOutboxService.build(
                updated.reviewId(),
                updated.orderItemId(),
                updated.sellerId(),
                updated.buyerId(),
                existing.rating(),
                updated.rating(),
                ratingChanged,
                now
        ));

        return new UpdateProductReviewResult(
                updated.reviewId(),
                updated.orderItemId(),
                updated.sellerId(),
                updated.buyerId(),
                updated.rating(),
                updated.comment(),
                updated.status(),
                ratingChanged,
                updated.createdAt(),
                updated.updatedAt(),
                ratingSummary.ratingAvg(),
                ratingSummary.ratingCount()
        );
    }

    public String successMessage() {
        return "Cap nhat danh gia thanh cong.";
    }

    private void validateHasUpdates(UpdateProductReviewCommand command) {
        if (command.rating() == null && command.comment() == null) {
            throw new AppException(
                    ErrorCode.VALIDATION_ERROR,
                    "Validation failed",
                    "request",
                    "at least one of rating or comment must be provided"
            );
        }
    }

    private void validateRating(int rating) {
        if (rating < MIN_RATING || rating > MAX_RATING) {
            throw new AppException(
                    ErrorCode.INVALID_RATING,
                    "Rating must be between " + MIN_RATING + " and " + MAX_RATING,
                    "rating",
                    "must be between " + MIN_RATING + " and " + MAX_RATING
            );
        }
    }

    private String resolveComment(String requestedComment, String existingComment) {
        if (requestedComment == null) {
            return existingComment;
        }
        if (!StringUtils.hasText(requestedComment)) {
            return null;
        }
        return requestedComment.trim();
    }

}
