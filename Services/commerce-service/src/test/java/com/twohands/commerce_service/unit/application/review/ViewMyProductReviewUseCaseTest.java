package com.twohands.commerce_service.unit.application.review;

import com.twohands.commerce_service.application.review.viewmyproductreview.ViewMyProductReviewCommand;
import com.twohands.commerce_service.application.review.viewmyproductreview.ViewMyProductReviewUseCase;
import com.twohands.commerce_service.domain.review.MyProductReviewSnapshot;
import com.twohands.commerce_service.domain.review.ReviewStatus;
import com.twohands.commerce_service.domain.review.ViewMyProductReviewRepository;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ViewMyProductReviewUseCaseTest {

    @Mock
    private ViewMyProductReviewRepository viewMyProductReviewRepository;

    private ViewMyProductReviewUseCase useCase;

    private final UUID buyerId = UUID.randomUUID();
    private final UUID productId = UUID.randomUUID();
    private final Instant now = Instant.parse("2026-05-21T10:00:00Z");

    @BeforeEach
    void setUp() {
        useCase = new ViewMyProductReviewUseCase(
                viewMyProductReviewRepository,
                Clock.fixed(now, ZoneOffset.UTC)
        );
    }

    @Test
    void shouldReturnNoReviewWhenBuyerHasNotReviewedProduct() {
        when(viewMyProductReviewRepository.isProductBuyerVisible(productId, now)).thenReturn(true);
        when(viewMyProductReviewRepository.findBuyerReviewForProduct(buyerId, productId))
                .thenReturn(Optional.empty());

        MyProductReviewSnapshot snapshot = useCase.execute(new ViewMyProductReviewCommand(buyerId, productId));

        assertThat(snapshot.hasReview()).isFalse();
        assertThat(snapshot.productId()).isEqualTo(productId);
        assertThat(snapshot.canEdit()).isFalse();
    }

    @Test
    void shouldReturnExistingReview() {
        UUID reviewId = UUID.randomUUID();
        when(viewMyProductReviewRepository.isProductBuyerVisible(productId, now)).thenReturn(true);
        when(viewMyProductReviewRepository.findBuyerReviewForProduct(buyerId, productId))
                .thenReturn(Optional.of(new MyProductReviewSnapshot(
                        true,
                        null,
                        reviewId,
                        UUID.randomUUID(),
                        5,
                        "Good",
                        ReviewStatus.VISIBLE,
                        now,
                        now,
                        true
                )));

        MyProductReviewSnapshot snapshot = useCase.execute(new ViewMyProductReviewCommand(buyerId, productId));

        assertThat(snapshot.hasReview()).isTrue();
        assertThat(snapshot.reviewId()).isEqualTo(reviewId);
        assertThat(snapshot.productId()).isEqualTo(productId);
        assertThat(snapshot.canEdit()).isTrue();
    }

    @Test
    void shouldThrowWhenProductNotVisible() {
        when(viewMyProductReviewRepository.isProductBuyerVisible(productId, now)).thenReturn(false);

        assertThatThrownBy(() -> useCase.execute(new ViewMyProductReviewCommand(buyerId, productId)))
                .isInstanceOf(AppException.class)
                .extracting(ex -> ((AppException) ex).getErrorCode())
                .isEqualTo(ErrorCode.PRODUCT_NOT_FOUND);
    }
}
