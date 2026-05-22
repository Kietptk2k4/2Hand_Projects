package com.twohands.commerce_service.unit.application.review;

import com.twohands.commerce_service.application.review.common.ReviewUpdatedOutboxService;
import com.twohands.commerce_service.application.review.updateproductreview.UpdateProductReviewCommand;
import com.twohands.commerce_service.application.review.updateproductreview.UpdateProductReviewUseCase;
import com.twohands.commerce_service.domain.outbox.OutboxEvent;
import com.twohands.commerce_service.domain.outbox.OutboxEventRepository;
import com.twohands.commerce_service.domain.review.ReviewStatus;
import com.twohands.commerce_service.domain.review.SellerRatingSummary;
import com.twohands.commerce_service.domain.review.UpdateProductReviewDraft;
import com.twohands.commerce_service.domain.review.UpdateProductReviewRepository;
import com.twohands.commerce_service.domain.review.UpdateProductReviewResult;
import com.twohands.commerce_service.domain.review.UpdateProductReviewSnapshot;
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
class UpdateProductReviewUseCaseTest {

    @Mock
    private UpdateProductReviewRepository updateProductReviewRepository;

    @Mock
    private OutboxEventRepository outboxEventRepository;

    @Mock
    private ReviewUpdatedOutboxService reviewUpdatedOutboxService;

    private UpdateProductReviewUseCase useCase;

    private final UUID buyerId = UUID.randomUUID();
    private final UUID sellerId = UUID.randomUUID();
    private final UUID reviewId = UUID.randomUUID();
    private final UUID orderItemId = UUID.randomUUID();
    private final Instant now = Instant.parse("2026-05-21T10:00:00Z");

    @BeforeEach
    void setUp() {
        useCase = new UpdateProductReviewUseCase(
                updateProductReviewRepository,
                outboxEventRepository,
                reviewUpdatedOutboxService,
                Clock.fixed(now, ZoneOffset.UTC)
        );
    }

    @Test
    void shouldUpdateRatingAndRecalculateSellerSummary() {
        when(updateProductReviewRepository.findByIdAndBuyerId(reviewId, buyerId))
                .thenReturn(Optional.of(visibleReview(4)));
        when(updateProductReviewRepository.updateReview(any(UpdateProductReviewDraft.class), eq(now)))
                .thenReturn(updatedResult(5));
        when(updateProductReviewRepository.recalculateSellerRating(sellerId, now))
                .thenReturn(new SellerRatingSummary(new BigDecimal("4.80"), 5));
        when(reviewUpdatedOutboxService.build(any(), any(), any(), any(), eq(4), eq(5), eq(true), any()))
                .thenReturn(sampleOutbox());

        UpdateProductReviewResult result = useCase.execute(
                new UpdateProductReviewCommand(buyerId, reviewId, 5, null)
        );

        assertThat(result.rating()).isEqualTo(5);
        assertThat(result.ratingChanged()).isTrue();
        assertThat(result.sellerRatingCount()).isEqualTo(5);
        verify(updateProductReviewRepository).recalculateSellerRating(sellerId, now);
        verify(outboxEventRepository).save(any(OutboxEvent.class));
    }

    @Test
    void shouldUpdateCommentWithoutRecalculatingRating() {
        when(updateProductReviewRepository.findByIdAndBuyerId(reviewId, buyerId))
                .thenReturn(Optional.of(visibleReview(4)));
        when(updateProductReviewRepository.updateReview(any(UpdateProductReviewDraft.class), eq(now)))
                .thenReturn(updatedResult(4));
        when(updateProductReviewRepository.loadSellerRatingSummary(sellerId))
                .thenReturn(new SellerRatingSummary(new BigDecimal("4.50"), 4));
        when(reviewUpdatedOutboxService.build(any(), any(), any(), any(), eq(4), eq(4), eq(false), any()))
                .thenReturn(sampleOutbox());

        UpdateProductReviewResult result = useCase.execute(
                new UpdateProductReviewCommand(buyerId, reviewId, null, "Updated comment")
        );

        assertThat(result.comment()).isEqualTo("Updated comment");
        assertThat(result.ratingChanged()).isFalse();
        verify(updateProductReviewRepository, never()).recalculateSellerRating(any(), any());
    }

    @Test
    void shouldRejectHiddenReview() {
        when(updateProductReviewRepository.findByIdAndBuyerId(reviewId, buyerId))
                .thenReturn(Optional.of(new UpdateProductReviewSnapshot(
                        reviewId, orderItemId, sellerId, buyerId, 4, "c", ReviewStatus.HIDDEN
                )));

        assertThatThrownBy(() -> useCase.execute(
                new UpdateProductReviewCommand(buyerId, reviewId, 5, null)
        ))
                .isInstanceOf(AppException.class)
                .extracting(ex -> ((AppException) ex).getErrorCode())
                .isEqualTo(ErrorCode.REVIEW_NOT_VISIBLE);

        verify(updateProductReviewRepository, never()).updateReview(any(), any());
    }

    @Test
    void shouldRejectWhenNoFieldsProvided() {
        assertThatThrownBy(() -> useCase.execute(
                new UpdateProductReviewCommand(buyerId, reviewId, null, null)
        ))
                .isInstanceOf(AppException.class)
                .extracting(ex -> ((AppException) ex).getErrorCode())
                .isEqualTo(ErrorCode.VALIDATION_ERROR);
    }

    @Test
    void shouldRejectInvalidRating() {
        when(updateProductReviewRepository.findByIdAndBuyerId(reviewId, buyerId))
                .thenReturn(Optional.of(visibleReview(4)));

        assertThatThrownBy(() -> useCase.execute(
                new UpdateProductReviewCommand(buyerId, reviewId, 6, null)
        ))
                .isInstanceOf(AppException.class)
                .extracting(ex -> ((AppException) ex).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_RATING);
    }

    @Test
    void shouldRejectWhenReviewNotOwned() {
        when(updateProductReviewRepository.findByIdAndBuyerId(reviewId, buyerId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(
                new UpdateProductReviewCommand(buyerId, reviewId, 5, null)
        ))
                .isInstanceOf(AppException.class)
                .extracting(ex -> ((AppException) ex).getErrorCode())
                .isEqualTo(ErrorCode.REVIEW_NOT_FOUND);
    }

    private UpdateProductReviewSnapshot visibleReview(int rating) {
        return new UpdateProductReviewSnapshot(
                reviewId, orderItemId, sellerId, buyerId, rating, "old", ReviewStatus.VISIBLE
        );
    }

    private UpdateProductReviewResult updatedResult(int rating) {
        return new UpdateProductReviewResult(
                reviewId,
                orderItemId,
                sellerId,
                buyerId,
                rating,
                "Updated comment",
                ReviewStatus.VISIBLE,
                false,
                now.minusSeconds(3600),
                now,
                null,
                0
        );
    }

    private OutboxEvent sampleOutbox() {
        return new OutboxEvent(
                UUID.randomUUID(),
                ReviewUpdatedOutboxService.EVENT_TYPE,
                "review:test:updated",
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
