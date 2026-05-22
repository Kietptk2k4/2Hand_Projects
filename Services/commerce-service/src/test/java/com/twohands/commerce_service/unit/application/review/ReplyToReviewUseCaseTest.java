package com.twohands.commerce_service.unit.application.review;

import com.twohands.commerce_service.application.review.common.ReviewRepliedOutboxService;
import com.twohands.commerce_service.application.review.replytoreview.ReplyToReviewCommand;
import com.twohands.commerce_service.application.review.replytoreview.ReplyToReviewUseCase;
import com.twohands.commerce_service.domain.outbox.OutboxEvent;
import com.twohands.commerce_service.domain.outbox.OutboxEventRepository;
import com.twohands.commerce_service.domain.review.ReplyToReviewRepository;
import com.twohands.commerce_service.domain.review.ReplyToReviewResult;
import com.twohands.commerce_service.domain.review.ReviewForSellerReply;
import com.twohands.commerce_service.domain.review.ReviewStatus;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReplyToReviewUseCaseTest {

    @Mock
    private ReplyToReviewRepository replyToReviewRepository;

    @Mock
    private OutboxEventRepository outboxEventRepository;

    @Mock
    private ReviewRepliedOutboxService reviewRepliedOutboxService;

    private ReplyToReviewUseCase useCase;

    private final UUID sellerId = UUID.randomUUID();
    private final UUID buyerId = UUID.randomUUID();
    private final UUID reviewId = UUID.randomUUID();
    private final Instant now = Instant.parse("2026-05-21T10:00:00Z");

    @BeforeEach
    void setUp() {
        useCase = new ReplyToReviewUseCase(
                replyToReviewRepository,
                outboxEventRepository,
                reviewRepliedOutboxService,
                Clock.fixed(now, ZoneOffset.UTC)
        );
    }

    @Test
    void shouldCreateReplyForVisibleReview() {
        ReviewForSellerReply review = visibleReview();
        ReplyToReviewResult created = new ReplyToReviewResult(
                UUID.randomUUID(), reviewId, sellerId, "Thank you!", now
        );

        when(replyToReviewRepository.findReviewById(reviewId)).thenReturn(Optional.of(review));
        when(replyToReviewRepository.existsReplyByReviewId(reviewId)).thenReturn(false);
        when(replyToReviewRepository.insertReply(eq(reviewId), eq(sellerId), eq("Thank you!"), eq(now)))
                .thenReturn(created);
        when(reviewRepliedOutboxService.build(any(), any(), any(), any(), any()))
                .thenReturn(sampleOutbox());

        ReplyToReviewResult result = useCase.execute(
                new ReplyToReviewCommand(sellerId, reviewId, "Thank you!")
        );

        assertThat(result.content()).isEqualTo("Thank you!");
        verify(outboxEventRepository).save(any(OutboxEvent.class));
    }

    @Test
    void shouldRejectWhenReviewNotOwned() {
        ReviewForSellerReply review = new ReviewForSellerReply(
                reviewId, UUID.randomUUID(), buyerId, ReviewStatus.VISIBLE
        );
        when(replyToReviewRepository.findReviewById(reviewId)).thenReturn(Optional.of(review));

        assertThatThrownBy(() -> useCase.execute(new ReplyToReviewCommand(sellerId, reviewId, "Thanks")))
                .isInstanceOf(AppException.class)
                .extracting(ex -> ((AppException) ex).getErrorCode())
                .isEqualTo(ErrorCode.REVIEW_NOT_FOUND);
    }

    @Test
    void shouldRejectHiddenReview() {
        ReviewForSellerReply review = new ReviewForSellerReply(
                reviewId, sellerId, buyerId, ReviewStatus.HIDDEN
        );
        when(replyToReviewRepository.findReviewById(reviewId)).thenReturn(Optional.of(review));

        assertThatThrownBy(() -> useCase.execute(new ReplyToReviewCommand(sellerId, reviewId, "Thanks")))
                .isInstanceOf(AppException.class)
                .extracting(ex -> ((AppException) ex).getErrorCode())
                .isEqualTo(ErrorCode.REVIEW_NOT_VISIBLE);
    }

    @Test
    void shouldRejectDuplicateReply() {
        when(replyToReviewRepository.findReviewById(reviewId)).thenReturn(Optional.of(visibleReview()));
        when(replyToReviewRepository.existsReplyByReviewId(reviewId)).thenReturn(true);

        assertThatThrownBy(() -> useCase.execute(new ReplyToReviewCommand(sellerId, reviewId, "Thanks")))
                .isInstanceOf(AppException.class)
                .extracting(ex -> ((AppException) ex).getErrorCode())
                .isEqualTo(ErrorCode.REVIEW_REPLY_EXISTS);

        verify(replyToReviewRepository, never()).insertReply(any(), any(), any(), any());
    }

    @Test
    void shouldRejectBlankContent() {
        assertThatThrownBy(() -> useCase.execute(new ReplyToReviewCommand(sellerId, reviewId, "  ")))
                .isInstanceOf(AppException.class)
                .extracting(ex -> ((AppException) ex).getErrorCode())
                .isEqualTo(ErrorCode.VALIDATION_ERROR);
    }

    private ReviewForSellerReply visibleReview() {
        return new ReviewForSellerReply(reviewId, sellerId, buyerId, ReviewStatus.VISIBLE);
    }

    private OutboxEvent sampleOutbox() {
        return new OutboxEvent(
                UUID.randomUUID(),
                ReviewRepliedOutboxService.EVENT_TYPE,
                "review:test:replied",
                reviewId,
                "commerce",
                "{}",
                com.twohands.commerce_service.domain.outbox.OutboxStatus.PENDING,
                0,
                now,
                null,
                null
        );
    }
}
