package com.twohands.commerce_service.unit.application.review;

import com.twohands.commerce_service.application.review.common.ReviewBuyerEnrichmentService;
import com.twohands.commerce_service.application.review.viewpublicshopreviews.ViewPublicShopReviewsCommand;
import com.twohands.commerce_service.application.review.viewpublicshopreviews.ViewPublicShopReviewsUseCase;
import com.twohands.commerce_service.common.pagination.PageMeta;
import com.twohands.commerce_service.domain.review.ProductReviewRatingSummary;
import com.twohands.commerce_service.domain.review.ProductReviewSort;
import com.twohands.commerce_service.domain.review.PublicShopReviewListItem;
import com.twohands.commerce_service.domain.review.ViewPublicShopReviewsRepository;
import com.twohands.commerce_service.domain.review.ViewPublicShopReviewsResult;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ViewPublicShopReviewsUseCaseTest {

    @Mock
    private ViewPublicShopReviewsRepository viewPublicShopReviewsRepository;

    @Mock
    private ReviewBuyerEnrichmentService reviewBuyerEnrichmentService;

    private ViewPublicShopReviewsUseCase useCase;

    private final UUID shopId = UUID.randomUUID();
    private final UUID buyerId = UUID.randomUUID();
    private final Instant now = Instant.parse("2026-05-21T10:00:00Z");

    @BeforeEach
    void setUp() {
        useCase = new ViewPublicShopReviewsUseCase(
                viewPublicShopReviewsRepository,
                reviewBuyerEnrichmentService
        );
        lenient().when(reviewBuyerEnrichmentService.enrichShopReviews(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void shouldReturnVisibleShopReviews() {
        when(viewPublicShopReviewsRepository.findVisibleShopReviews(
                eq(shopId),
                eq(5),
                eq(ProductReviewSort.NEWEST),
                any()
        )).thenReturn(Optional.of(sampleResult()));

        ViewPublicShopReviewsResult result = useCase.execute(
                new ViewPublicShopReviewsCommand(shopId, 1, 20, 5, null)
        );

        assertThat(result.shopId()).isEqualTo(shopId);
        assertThat(result.shopName()).isEqualTo("Test Shop");
        assertThat(result.ratingSummary().ratingCount()).isEqualTo(2);
        assertThat(result.reviews()).hasSize(1);
        assertThat(result.reviews().getFirst().productNameSnapshot()).isEqualTo("Vintage Jacket");
        assertThat(result.pagination().totalItems()).isEqualTo(1);
    }

    @Test
    void shouldThrowWhenShopNotActive() {
        when(viewPublicShopReviewsRepository.findVisibleShopReviews(
                eq(shopId),
                eq(null),
                eq(ProductReviewSort.NEWEST),
                any()
        )).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(new ViewPublicShopReviewsCommand(shopId, 1, 20, null, null)))
                .isInstanceOf(AppException.class)
                .extracting(ex -> ((AppException) ex).getErrorCode())
                .isEqualTo(ErrorCode.SHOP_NOT_FOUND);
    }

    @Test
    void shouldRejectInvalidRatingFilter() {
        assertThatThrownBy(() -> useCase.execute(new ViewPublicShopReviewsCommand(shopId, 1, 20, 6, null)))
                .isInstanceOf(AppException.class)
                .extracting(ex -> ((AppException) ex).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_RATING);

        verify(viewPublicShopReviewsRepository, never())
                .findVisibleShopReviews(any(), any(), any(), any());
    }

    @Test
    void shouldRejectInvalidSort() {
        assertThatThrownBy(() -> useCase.execute(new ViewPublicShopReviewsCommand(shopId, 1, 20, null, "bad")))
                .isInstanceOf(AppException.class)
                .extracting(ex -> ((AppException) ex).getErrorCode())
                .isEqualTo(ErrorCode.VALIDATION_ERROR);
    }

    private ViewPublicShopReviewsResult sampleResult() {
        return new ViewPublicShopReviewsResult(
                shopId,
                "Test Shop",
                "http://avatar",
                UUID.randomUUID(),
                new ProductReviewRatingSummary(BigDecimal.valueOf(4.5), 2),
                List.of(new PublicShopReviewListItem(
                        UUID.randomUUID(),
                        buyerId,
                        "Buyer",
                        null,
                        "Vintage Jacket",
                        5,
                        "Great shop",
                        now,
                        List.of(),
                        null
                )),
                PageMeta.of(1, 20, 1)
        );
    }
}
