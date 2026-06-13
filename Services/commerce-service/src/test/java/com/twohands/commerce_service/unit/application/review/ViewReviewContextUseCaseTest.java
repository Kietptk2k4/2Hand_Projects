package com.twohands.commerce_service.unit.application.review;

import com.twohands.commerce_service.application.review.viewreviewcontext.ViewReviewContextCommand;
import com.twohands.commerce_service.application.review.viewreviewcontext.ViewReviewContextUseCase;
import com.twohands.commerce_service.domain.review.ReviewContextSnapshot;
import com.twohands.commerce_service.domain.review.ViewReviewContextRepository;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ViewReviewContextUseCaseTest {

    @Mock
    private ViewReviewContextRepository viewReviewContextRepository;

    @InjectMocks
    private ViewReviewContextUseCase useCase;

    private final UUID buyerId = UUID.randomUUID();
    private final UUID orderItemId = UUID.randomUUID();

    @Test
    void shouldReturnReviewContextForOwnedOrderItem() {
        ReviewContextSnapshot snapshot = sampleSnapshot();
        when(viewReviewContextRepository.findByOrderItemIdAndBuyerId(orderItemId, buyerId))
                .thenReturn(Optional.of(snapshot));

        ReviewContextSnapshot result = useCase.execute(new ViewReviewContextCommand(buyerId, orderItemId));

        assertThat(result).isEqualTo(snapshot);
        assertThat(result.hasReview()).isFalse();
    }

    @Test
    void shouldRejectMissingOrderItemId() {
        assertThatThrownBy(() -> useCase.execute(new ViewReviewContextCommand(buyerId, null)))
                .isInstanceOf(AppException.class)
                .extracting(ex -> ((AppException) ex).getErrorCode())
                .isEqualTo(ErrorCode.VALIDATION_ERROR);
    }

    @Test
    void shouldRejectUnknownOrderItem() {
        when(viewReviewContextRepository.findByOrderItemIdAndBuyerId(orderItemId, buyerId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(new ViewReviewContextCommand(buyerId, orderItemId)))
                .isInstanceOf(AppException.class)
                .extracting(ex -> ((AppException) ex).getErrorCode())
                .isEqualTo(ErrorCode.ORDER_ITEM_NOT_FOUND);
    }

    private ReviewContextSnapshot sampleSnapshot() {
        return new ReviewContextSnapshot(
                orderItemId,
                UUID.randomUUID(),
                UUID.randomUUID(),
                "COMPLETED",
                "iPhone 15",
                "https://cdn.example/p1.jpg",
                "Tech Shop",
                BigDecimal.valueOf(1_000_000),
                Instant.parse("2026-05-20T10:00:00Z"),
                false,
                null
        );
    }
}
