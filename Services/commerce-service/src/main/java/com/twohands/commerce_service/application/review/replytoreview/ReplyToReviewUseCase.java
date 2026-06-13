package com.twohands.commerce_service.application.review.replytoreview;

import com.twohands.commerce_service.application.review.common.ReviewRepliedOutboxService;
import com.twohands.commerce_service.domain.outbox.OutboxEventRepository;
import com.twohands.commerce_service.domain.review.ReplyToReviewRepository;
import com.twohands.commerce_service.domain.review.ReplyToReviewResult;
import com.twohands.commerce_service.domain.review.ReviewForSellerReply;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Clock;
import java.time.Instant;

@Service
public class ReplyToReviewUseCase {

    private final ReplyToReviewRepository replyToReviewRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final ReviewRepliedOutboxService reviewRepliedOutboxService;
    private final Clock clock;

    public ReplyToReviewUseCase(
            ReplyToReviewRepository replyToReviewRepository,
            OutboxEventRepository outboxEventRepository,
            ReviewRepliedOutboxService reviewRepliedOutboxService,
            Clock clock
    ) {
        this.replyToReviewRepository = replyToReviewRepository;
        this.outboxEventRepository = outboxEventRepository;
        this.reviewRepliedOutboxService = reviewRepliedOutboxService;
        this.clock = clock;
    }

    @Transactional
    public ReplyToReviewResult execute(ReplyToReviewCommand command) {
        String content = validateContent(command.content());

        ReviewForSellerReply review = replyToReviewRepository.findReviewById(command.reviewId())
                .filter(found -> found.isOwnedBy(command.sellerId()))
                .orElseThrow(() -> new AppException(ErrorCode.REVIEW_NOT_FOUND));

        if (!review.isVisible()) {
            throw new AppException(ErrorCode.REVIEW_NOT_VISIBLE, "Hidden review cannot be replied to");
        }

        if (replyToReviewRepository.existsReplyByReviewId(command.reviewId())) {
            throw new AppException(ErrorCode.REVIEW_REPLY_EXISTS);
        }

        Instant now = clock.instant();
        ReplyToReviewResult created = replyToReviewRepository.insertReply(
                command.reviewId(),
                command.sellerId(),
                content,
                now
        );

        outboxEventRepository.save(reviewRepliedOutboxService.build(
                created.replyId(),
                created.reviewId(),
                created.sellerId(),
                review.buyerId(),
                review.productId(),
                now
        ));

        return created;
    }

    public String successMessage() {
        return "Phan hoi danh gia thanh cong.";
    }

    private String validateContent(String content) {
        if (!StringUtils.hasText(content)) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "content is required", "content", "must not be blank");
        }
        return content.trim();
    }
}
