package com.twohands.commerce_service.application.review.createproductreview;

import com.twohands.commerce_service.application.review.common.ReviewCreatedOutboxService;
import com.twohands.commerce_service.domain.outbox.OutboxEventRepository;
import com.twohands.commerce_service.domain.review.CreateProductReviewDraft;
import com.twohands.commerce_service.domain.review.CreateProductReviewRepository;
import com.twohands.commerce_service.domain.review.CreateProductReviewResult;
import com.twohands.commerce_service.domain.review.ReviewableOrderItem;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Clock;
import java.time.Instant;

@Service
public class CreateProductReviewUseCase {

    private static final int MIN_RATING = 1;
    private static final int MAX_RATING = 5;

    private final CreateProductReviewRepository createProductReviewRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final ReviewCreatedOutboxService reviewCreatedOutboxService;
    private final Clock clock;

    public CreateProductReviewUseCase(
            CreateProductReviewRepository createProductReviewRepository,
            OutboxEventRepository outboxEventRepository,
            ReviewCreatedOutboxService reviewCreatedOutboxService,
            Clock clock
    ) {
        this.createProductReviewRepository = createProductReviewRepository;
        this.outboxEventRepository = outboxEventRepository;
        this.reviewCreatedOutboxService = reviewCreatedOutboxService;
        this.clock = clock;
    }

    @Transactional
    public CreateProductReviewResult execute(CreateProductReviewCommand command) {
        validateRating(command.rating());

        ReviewableOrderItem orderItem = createProductReviewRepository
                .findReviewableOrderItem(command.orderItemId(), command.buyerId())
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_ITEM_NOT_FOUND));

        if (!orderItem.isCompleted()) {
            throw new AppException(
                    ErrorCode.ORDER_ITEM_NOT_REVIEWABLE,
                    "Order item must be COMPLETED before reviewing"
            );
        }

        if (createProductReviewRepository.existsByOrderItemId(command.orderItemId())) {
            throw new AppException(ErrorCode.REVIEW_ALREADY_EXISTS);
        }

        Instant now = clock.instant();
        String comment = normalizeComment(command.comment());

        CreateProductReviewResult created = createProductReviewRepository.createReview(
                new CreateProductReviewDraft(
                        orderItem.orderItemId(),
                        command.buyerId(),
                        orderItem.sellerId(),
                        command.rating(),
                        comment
                ),
                now
        );

        outboxEventRepository.save(reviewCreatedOutboxService.build(
                created.reviewId(),
                created.orderItemId(),
                created.sellerId(),
                created.buyerId(),
                created.rating(),
                now
        ));

        return created;
    }

    public String successMessage() {
        return "Tao danh gia san pham thanh cong.";
    }

    private void validateRating(Integer rating) {
        if (rating == null) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "rating is required", "rating", "must not be null");
        }
        if (rating < MIN_RATING || rating > MAX_RATING) {
            throw new AppException(
                    ErrorCode.INVALID_RATING,
                    "Rating must be between " + MIN_RATING + " and " + MAX_RATING,
                    "rating",
                    "must be between " + MIN_RATING + " and " + MAX_RATING
            );
        }
    }

    private String normalizeComment(String comment) {
        if (!StringUtils.hasText(comment)) {
            return null;
        }
        return comment.trim();
    }
}
