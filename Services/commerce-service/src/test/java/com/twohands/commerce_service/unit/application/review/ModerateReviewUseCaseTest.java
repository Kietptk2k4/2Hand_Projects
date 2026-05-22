package com.twohands.commerce_service.unit.application.review;

import com.twohands.commerce_service.application.review.common.ReviewHiddenOutboxService;
import com.twohands.commerce_service.application.review.common.ReviewRestoredOutboxService;
import com.twohands.commerce_service.application.review.moderatereview.ModerateReviewCommand;
import com.twohands.commerce_service.application.review.moderatereview.ModerateReviewUseCase;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
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
class ModerateReviewUseCaseTest {

    @Mock
    private ModerateReviewRepository moderateReviewRepository;

    @Mock
    private OutboxEventRepository outboxEventRepository;

    @Mock
    private ReviewHiddenOutboxService reviewHiddenOutboxService;

    @Mock
    private ReviewRestoredOutboxService reviewRestoredOutboxService;

    private ModerateReviewUseCase useCase;

    private final UUID adminId = UUID.randomUUID();
    private final UUID reviewId = UUID.randomUUID();
    private final UUID sellerId = UUID.randomUUID();
    private final Instant now = Instant.parse("2026-05-21T10:00:00Z");

    @BeforeEach
    void setUp() {
        useCase = new ModerateReviewUseCase(
                moderateReviewRepository,
                outboxEventRepository,
                reviewHiddenOutboxService,
                reviewRestoredOutboxService,
                Clock.fixed(now, ZoneOffset.UTC)
        );
    }

    @Test
    void hidesVisibleReviewAndRecalculatesRating() {
        ReviewForModeration review = visibleReview();
        when(moderateReviewRepository.findById(reviewId)).thenReturn(Optional.of(review));
        when(moderateReviewRepository.updateStatus(
                reviewId, ReviewStatus.VISIBLE, ReviewStatus.HIDDEN, now))
                .thenReturn(true);
        when(moderateReviewRepository.recalculateSellerRating(sellerId, now))
                .thenReturn(new SellerRatingSummary(BigDecimal.valueOf(4.5), 2));
        when(reviewHiddenOutboxService.build(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(sampleOutbox());

        ModerateReviewResult result = useCase.execute(new ModerateReviewCommand(
                adminId, reviewId, ReviewModerationAction.HIDE, "Spam content"
        ));

        assertThat(result.status()).isEqualTo(ReviewStatus.HIDDEN);
        assertThat(result.previousStatus()).isEqualTo(ReviewStatus.VISIBLE);
        assertThat(result.alreadyModerated()).isFalse();
        verify(outboxEventRepository).save(any(OutboxEvent.class));
    }

    @Test
    void restoresHiddenReview() {
        ReviewForModeration review = new ReviewForModeration(
                reviewId, UUID.randomUUID(), sellerId, UUID.randomUUID(), 5, ReviewStatus.HIDDEN
        );
        when(moderateReviewRepository.findById(reviewId)).thenReturn(Optional.of(review));
        when(moderateReviewRepository.updateStatus(
                reviewId, ReviewStatus.HIDDEN, ReviewStatus.VISIBLE, now))
                .thenReturn(true);
        when(moderateReviewRepository.recalculateSellerRating(sellerId, now))
                .thenReturn(new SellerRatingSummary(BigDecimal.valueOf(5.0), 1));
        when(reviewRestoredOutboxService.build(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(sampleOutbox());

        ModerateReviewResult result = useCase.execute(new ModerateReviewCommand(
                adminId, reviewId, ReviewModerationAction.RESTORE, "False positive"
        ));

        assertThat(result.status()).isEqualTo(ReviewStatus.VISIBLE);
        verify(reviewRestoredOutboxService).build(any(), any(), any(), any(), any(), any(), eq(now));
    }

    @Test
    void idempotentWhenAlreadyHidden() {
        ReviewForModeration review = new ReviewForModeration(
                reviewId, UUID.randomUUID(), sellerId, UUID.randomUUID(), 4, ReviewStatus.HIDDEN
        );
        when(moderateReviewRepository.findById(reviewId)).thenReturn(Optional.of(review));
        when(moderateReviewRepository.recalculateSellerRating(sellerId, now))
                .thenReturn(new SellerRatingSummary(BigDecimal.ZERO, 0));

        ModerateReviewResult result = useCase.execute(new ModerateReviewCommand(
                adminId, reviewId, ReviewModerationAction.HIDE, "Already hidden"
        ));

        assertThat(result.alreadyModerated()).isTrue();
        verify(moderateReviewRepository, never()).updateStatus(any(), any(), any(), any());
        verify(outboxEventRepository, never()).save(any());
    }

    @Test
    void rejectsBlankReason() {
        assertThatThrownBy(() -> useCase.execute(new ModerateReviewCommand(
                adminId, reviewId, ReviewModerationAction.HIDE, "  "
        )))
                .isInstanceOf(AppException.class)
                .extracting(ex -> ((AppException) ex).getErrorCode())
                .isEqualTo(ErrorCode.VALIDATION_ERROR);
    }

    @Test
    void reviewNotFound() {
        when(moderateReviewRepository.findById(reviewId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(new ModerateReviewCommand(
                adminId, reviewId, ReviewModerationAction.HIDE, "reason"
        )))
                .isInstanceOf(AppException.class)
                .extracting(ex -> ((AppException) ex).getErrorCode())
                .isEqualTo(ErrorCode.REVIEW_NOT_FOUND);
    }

    private ReviewForModeration visibleReview() {
        return new ReviewForModeration(
                reviewId, UUID.randomUUID(), sellerId, UUID.randomUUID(), 5, ReviewStatus.VISIBLE
        );
    }

    private OutboxEvent sampleOutbox() {
        return new OutboxEvent(
                UUID.randomUUID(),
                ReviewHiddenOutboxService.EVENT_TYPE,
                "review:test:hidden",
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
