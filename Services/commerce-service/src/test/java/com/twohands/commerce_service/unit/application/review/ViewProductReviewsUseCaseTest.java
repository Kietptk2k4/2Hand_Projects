package com.twohands.commerce_service.unit.application.review;

import com.twohands.commerce_service.application.review.viewproductreviews.ViewProductReviewsCommand;
import com.twohands.commerce_service.application.review.viewproductreviews.ViewProductReviewsUseCase;
import com.twohands.commerce_service.common.pagination.PageMeta;
import com.twohands.commerce_service.domain.review.ProductReviewListItem;
import com.twohands.commerce_service.domain.review.ProductReviewRatingSummary;
import com.twohands.commerce_service.domain.review.ProductReviewSort;
import com.twohands.commerce_service.domain.review.ViewProductReviewsRepository;
import com.twohands.commerce_service.domain.review.ViewProductReviewsResult;
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
import java.util.List;
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
class ViewProductReviewsUseCaseTest {

    @Mock
    private ViewProductReviewsRepository viewProductReviewsRepository;

    private ViewProductReviewsUseCase useCase;

    private final UUID productId = UUID.randomUUID();
    private final Instant now = Instant.parse("2026-05-21T10:00:00Z");

    @BeforeEach
    void setUp() {
        useCase = new ViewProductReviewsUseCase(
                viewProductReviewsRepository,
                Clock.fixed(now, ZoneOffset.UTC)
        );
    }

    @Test
    void shouldReturnVisibleProductReviews() {
        when(viewProductReviewsRepository.findVisibleProductReviews(
                eq(productId),
                eq(5),
                eq(ProductReviewSort.NEWEST),
                any(),
                eq(now)
        )).thenReturn(Optional.of(sampleResult()));

        ViewProductReviewsResult result = useCase.execute(
                new ViewProductReviewsCommand(productId, 1, 20, 5, null)
        );

        assertThat(result.productId()).isEqualTo(productId);
        assertThat(result.ratingSummary().ratingCount()).isEqualTo(2);
        assertThat(result.reviews()).hasSize(1);
        assertThat(result.pagination().totalItems()).isEqualTo(1);
    }

    @Test
    void shouldThrowWhenProductNotVisible() {
        when(viewProductReviewsRepository.findVisibleProductReviews(
                eq(productId),
                eq(null),
                eq(ProductReviewSort.NEWEST),
                any(),
                eq(now)
        )).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(new ViewProductReviewsCommand(productId, 1, 20, null, null)))
                .isInstanceOf(AppException.class)
                .extracting(ex -> ((AppException) ex).getErrorCode())
                .isEqualTo(ErrorCode.PRODUCT_NOT_FOUND);
    }

    @Test
    void shouldRejectInvalidRatingFilter() {
        assertThatThrownBy(() -> useCase.execute(new ViewProductReviewsCommand(productId, 1, 20, 6, null)))
                .isInstanceOf(AppException.class)
                .extracting(ex -> ((AppException) ex).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_RATING);

        verify(viewProductReviewsRepository, never())
                .findVisibleProductReviews(any(), any(), any(), any(), any());
    }

    @Test
    void shouldRejectInvalidSort() {
        assertThatThrownBy(() -> useCase.execute(new ViewProductReviewsCommand(productId, 1, 20, null, "bad")))
                .isInstanceOf(AppException.class)
                .extracting(ex -> ((AppException) ex).getErrorCode())
                .isEqualTo(ErrorCode.VALIDATION_ERROR);
    }

    private ViewProductReviewsResult sampleResult() {
        return new ViewProductReviewsResult(
                productId,
                new ProductReviewRatingSummary(BigDecimal.valueOf(4.5), 2),
                List.of(new ProductReviewListItem(
                        UUID.randomUUID(),
                        5,
                        "Great product",
                        now,
                        List.of(),
                        null
                )),
                PageMeta.of(1, 20, 1)
        );
    }
}
