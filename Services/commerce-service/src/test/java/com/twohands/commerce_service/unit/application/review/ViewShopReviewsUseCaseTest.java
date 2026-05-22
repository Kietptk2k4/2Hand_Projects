package com.twohands.commerce_service.unit.application.review;

import com.twohands.commerce_service.application.review.viewshopreviews.ViewShopReviewsCommand;
import com.twohands.commerce_service.application.review.viewshopreviews.ViewShopReviewsUseCase;
import com.twohands.commerce_service.common.pagination.PageMeta;
import com.twohands.commerce_service.domain.review.ProductReviewRatingSummary;
import com.twohands.commerce_service.domain.review.ProductReviewSort;
import com.twohands.commerce_service.domain.review.ReviewStatus;
import com.twohands.commerce_service.domain.review.ShopReviewListItem;
import com.twohands.commerce_service.domain.review.ViewShopReviewsRepository;
import com.twohands.commerce_service.domain.review.ViewShopReviewsResult;
import com.twohands.commerce_service.domain.shop.SellerShop;
import com.twohands.commerce_service.domain.shop.SellerShopRepository;
import com.twohands.commerce_service.domain.shop.ShopStatus;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ViewShopReviewsUseCaseTest {

    @Mock
    private SellerShopRepository sellerShopRepository;

    @Mock
    private ViewShopReviewsRepository viewShopReviewsRepository;

    private ViewShopReviewsUseCase useCase;

    private final UUID sellerId = UUID.randomUUID();
    private final UUID shopId = UUID.randomUUID();
    private final Instant now = Instant.parse("2026-05-21T10:00:00Z");

    @BeforeEach
    void setUp() {
        useCase = new ViewShopReviewsUseCase(sellerShopRepository, viewShopReviewsRepository);
    }

    @Test
    void shouldReturnShopReviewsForSeller() {
        when(sellerShopRepository.findBySellerId(sellerId))
                .thenReturn(Optional.of(new SellerShop(shopId, sellerId, ShopStatus.ACTIVE)));
        when(viewShopReviewsRepository.findShopReviews(
                eq(sellerId),
                eq(shopId),
                eq(5),
                eq(ReviewStatus.VISIBLE),
                eq(ProductReviewSort.NEWEST),
                any()
        )).thenReturn(sampleResult());

        ViewShopReviewsResult result = useCase.execute(
                new ViewShopReviewsCommand(sellerId, 1, 20, 5, null)
        );

        assertThat(result.shopId()).isEqualTo(shopId);
        assertThat(result.ratingSummary().ratingCount()).isEqualTo(2);
        assertThat(result.reviews()).hasSize(1);
        assertThat(result.pagination().totalItems()).isEqualTo(1);
    }

    @Test
    void shouldDefaultStatusFilterToVisible() {
        when(sellerShopRepository.findBySellerId(sellerId))
                .thenReturn(Optional.of(new SellerShop(shopId, sellerId, ShopStatus.ACTIVE)));
        when(viewShopReviewsRepository.findShopReviews(
                eq(sellerId),
                eq(shopId),
                eq(null),
                eq(ReviewStatus.VISIBLE),
                eq(ProductReviewSort.NEWEST),
                any()
        )).thenReturn(sampleResult());

        useCase.execute(new ViewShopReviewsCommand(sellerId, 1, 20, null, null));

        verify(viewShopReviewsRepository).findShopReviews(
                eq(sellerId),
                eq(shopId),
                eq(null),
                eq(ReviewStatus.VISIBLE),
                eq(ProductReviewSort.NEWEST),
                any()
        );
    }

    @Test
    void shouldThrowWhenSellerHasNoShop() {
        when(sellerShopRepository.findBySellerId(sellerId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(new ViewShopReviewsCommand(sellerId, 1, 20, null, null)))
                .isInstanceOf(AppException.class)
                .extracting(ex -> ((AppException) ex).getErrorCode())
                .isEqualTo(ErrorCode.SELLER_SHOP_NOT_FOUND);

        verify(viewShopReviewsRepository, never()).findShopReviews(any(), any(), any(), any(), any(), any());
    }

    @Test
    void shouldRejectInvalidRatingFilter() {
        when(sellerShopRepository.findBySellerId(sellerId))
                .thenReturn(Optional.of(new SellerShop(shopId, sellerId, ShopStatus.ACTIVE)));

        assertThatThrownBy(() -> useCase.execute(new ViewShopReviewsCommand(sellerId, 1, 20, 0, null)))
                .isInstanceOf(AppException.class)
                .extracting(ex -> ((AppException) ex).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_RATING);

        verify(viewShopReviewsRepository, never()).findShopReviews(any(), any(), any(), any(), any(), any());
    }

    @Test
    void shouldRejectInvalidStatusFilter() {
        when(sellerShopRepository.findBySellerId(sellerId))
                .thenReturn(Optional.of(new SellerShop(shopId, sellerId, ShopStatus.ACTIVE)));

        assertThatThrownBy(() -> useCase.execute(new ViewShopReviewsCommand(sellerId, 1, 20, null, "bad")))
                .isInstanceOf(AppException.class)
                .extracting(ex -> ((AppException) ex).getErrorCode())
                .isEqualTo(ErrorCode.VALIDATION_ERROR);
    }

    private ViewShopReviewsResult sampleResult() {
        return new ViewShopReviewsResult(
                shopId,
                new ProductReviewRatingSummary(BigDecimal.valueOf(4.5), 2),
                List.of(new ShopReviewListItem(
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        "iPhone 13",
                        5,
                        "Great",
                        ReviewStatus.VISIBLE,
                        now,
                        List.of(),
                        null
                )),
                PageMeta.of(1, 20, 1)
        );
    }
}
