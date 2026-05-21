package com.twohands.commerce_service.unit.application.review;

import com.twohands.commerce_service.application.review.common.ReviewCreatedOutboxService;
import com.twohands.commerce_service.application.review.createproductreview.CreateProductReviewCommand;
import com.twohands.commerce_service.application.review.createproductreview.CreateProductReviewUseCase;
import com.twohands.commerce_service.domain.outbox.OutboxEvent;
import com.twohands.commerce_service.domain.outbox.OutboxEventRepository;
import com.twohands.commerce_service.domain.review.CreateProductReviewDraft;
import com.twohands.commerce_service.domain.review.CreateProductReviewRepository;
import com.twohands.commerce_service.domain.review.CreateProductReviewResult;
import com.twohands.commerce_service.domain.review.ReviewStatus;
import com.twohands.commerce_service.domain.review.ReviewableOrderItem;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
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
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateProductReviewUseCaseTest {

    @Mock
    private CreateProductReviewRepository createProductReviewRepository;

    @Mock
    private OutboxEventRepository outboxEventRepository;

    @Mock
    private ReviewCreatedOutboxService reviewCreatedOutboxService;

    @InjectMocks
    private CreateProductReviewUseCase useCase;

    private final UUID buyerId = UUID.randomUUID();
    private final UUID sellerId = UUID.randomUUID();
    private final UUID orderItemId = UUID.randomUUID();
    private final UUID reviewId = UUID.randomUUID();
    private final Instant now = Instant.parse("2026-05-21T10:00:00Z");

    @BeforeEach
    void setUp() {
        useCase = new CreateProductReviewUseCase(
                createProductReviewRepository,
                outboxEventRepository,
                reviewCreatedOutboxService,
                Clock.fixed(now, ZoneOffset.UTC)
        );
    }

    @Test
    void shouldCreateReviewForCompletedOrderItem() {
        when(createProductReviewRepository.findReviewableOrderItem(orderItemId, buyerId))
                .thenReturn(Optional.of(completedOrderItem()));
        when(createProductReviewRepository.existsByOrderItemId(orderItemId)).thenReturn(false);
        when(createProductReviewRepository.createReview(any(CreateProductReviewDraft.class), eq(now)))
                .thenReturn(createdResult());
        when(reviewCreatedOutboxService.build(any(), any(), any(), any(), anyInt(), any()))
                .thenReturn(sampleOutbox());

        CreateProductReviewResult result = useCase.execute(new CreateProductReviewCommand(
                buyerId,
                orderItemId,
                5,
                "  Great product  "
        ));

        assertThat(result.rating()).isEqualTo(5);
        assertThat(result.status()).isEqualTo(ReviewStatus.VISIBLE);
        assertThat(result.sellerRatingCount()).isEqualTo(3);
        verify(outboxEventRepository).save(any(OutboxEvent.class));
    }

    @Test
    void shouldRejectWhenOrderItemNotFound() {
        when(createProductReviewRepository.findReviewableOrderItem(orderItemId, buyerId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(new CreateProductReviewCommand(buyerId, orderItemId, 5, null)))
                .isInstanceOf(AppException.class)
                .extracting(ex -> ((AppException) ex).getErrorCode())
                .isEqualTo(ErrorCode.ORDER_ITEM_NOT_FOUND);
    }

    @Test
    void shouldRejectWhenOrderItemNotCompleted() {
        ReviewableOrderItem delivered = new ReviewableOrderItem(
                orderItemId,
                UUID.randomUUID(),
                buyerId,
                sellerId,
                UUID.randomUUID(),
                "DELIVERED"
        );
        when(createProductReviewRepository.findReviewableOrderItem(orderItemId, buyerId))
                .thenReturn(Optional.of(delivered));

        assertThatThrownBy(() -> useCase.execute(new CreateProductReviewCommand(buyerId, orderItemId, 4, null)))
                .isInstanceOf(AppException.class)
                .extracting(ex -> ((AppException) ex).getErrorCode())
                .isEqualTo(ErrorCode.ORDER_ITEM_NOT_REVIEWABLE);

        verify(createProductReviewRepository, never()).createReview(any(), any());
    }

    @Test
    void shouldRejectDuplicateReview() {
        when(createProductReviewRepository.findReviewableOrderItem(orderItemId, buyerId))
                .thenReturn(Optional.of(completedOrderItem()));
        when(createProductReviewRepository.existsByOrderItemId(orderItemId)).thenReturn(true);

        assertThatThrownBy(() -> useCase.execute(new CreateProductReviewCommand(buyerId, orderItemId, 5, null)))
                .isInstanceOf(AppException.class)
                .extracting(ex -> ((AppException) ex).getErrorCode())
                .isEqualTo(ErrorCode.REVIEW_ALREADY_EXISTS);
    }

    @Test
    void shouldRejectInvalidRating() {
        assertThatThrownBy(() -> useCase.execute(new CreateProductReviewCommand(buyerId, orderItemId, 0, null)))
                .isInstanceOf(AppException.class)
                .extracting(ex -> ((AppException) ex).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_RATING);
    }

    private ReviewableOrderItem completedOrderItem() {
        return new ReviewableOrderItem(
                orderItemId,
                UUID.randomUUID(),
                buyerId,
                sellerId,
                UUID.randomUUID(),
                "COMPLETED"
        );
    }

    private CreateProductReviewResult createdResult() {
        return new CreateProductReviewResult(
                reviewId,
                orderItemId,
                sellerId,
                buyerId,
                5,
                "Great product",
                ReviewStatus.VISIBLE,
                now,
                new BigDecimal("4.67"),
                3
        );
    }

    private OutboxEvent sampleOutbox() {
        return new OutboxEvent(
                UUID.randomUUID(),
                ReviewCreatedOutboxService.EVENT_TYPE,
                "review:created",
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
