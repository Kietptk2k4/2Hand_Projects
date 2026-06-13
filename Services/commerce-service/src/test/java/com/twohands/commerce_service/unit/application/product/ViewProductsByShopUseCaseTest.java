package com.twohands.commerce_service.unit.application.product;

import com.twohands.commerce_service.application.product.viewproductsbyshop.ViewProductsByShopCommand;
import com.twohands.commerce_service.application.product.viewproductsbyshop.ViewProductsByShopUseCase;
import com.twohands.commerce_service.common.pagination.PageMeta;
import com.twohands.commerce_service.domain.discovery.ProductCardSummary;
import com.twohands.commerce_service.domain.discovery.ProductDiscoverySort;
import com.twohands.commerce_service.domain.discovery.PublicShopSummary;
import com.twohands.commerce_service.domain.discovery.ViewProductsByShopRepository;
import com.twohands.commerce_service.domain.discovery.ViewProductsByShopResult;
import com.twohands.commerce_service.domain.product.ProductStatus;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ViewProductsByShopUseCaseTest {

    @Mock
    private ViewProductsByShopRepository viewProductsByShopRepository;

    private ViewProductsByShopUseCase useCase;

    private final UUID shopId = UUID.randomUUID();
    private final Instant now = Instant.parse("2026-05-21T16:00:00Z");

    @BeforeEach
    void setUp() {
        useCase = new ViewProductsByShopUseCase(
                viewProductsByShopRepository,
                Clock.fixed(now, ZoneOffset.UTC)
        );
    }

    @Test
    void shouldReturnShopProductsWhenShopActive() {
        when(viewProductsByShopRepository.findVisibleProductsByShopId(
                eq(shopId),
                eq(ProductDiscoverySort.NEWEST),
                any(),
                eq(now)
        )).thenReturn(Optional.of(sampleResult()));

        ViewProductsByShopResult result = useCase.execute(new ViewProductsByShopCommand(shopId, 1, 20, null));

        assertThat(result.shop().shopId()).isEqualTo(shopId);
        assertThat(result.shop().shopVacation()).isFalse();
        assertThat(result.items()).hasSize(1);
        assertThat(result.pagination().totalItems()).isEqualTo(1);

        verify(viewProductsByShopRepository).findVisibleProductsByShopId(
                eq(shopId),
                eq(ProductDiscoverySort.NEWEST),
                any(),
                eq(now)
        );
    }

    @Test
    void shouldReturnEmptyItemsWhenShopHasNoVisibleProducts() {
        when(viewProductsByShopRepository.findVisibleProductsByShopId(
                eq(shopId),
                eq(ProductDiscoverySort.NEWEST),
                any(),
                eq(now)
        )).thenReturn(Optional.of(new ViewProductsByShopResult(
                new PublicShopSummary(
                        shopId,
                        "Tech Shop",
                        "Phones",
                        null,
                        null,
                        BigDecimal.valueOf(4.5),
                        10,
                        false,
                        null,
                        UUID.randomUUID()
                ),
                List.of(),
                PageMeta.of(1, 20, 0)
        )));

        ViewProductsByShopResult result = useCase.execute(new ViewProductsByShopCommand(shopId, 1, 20, null));

        assertThat(result.items()).isEmpty();
        assertThat(result.pagination().totalItems()).isZero();
    }

    @Test
    void shouldThrowWhenShopNotFoundOrNotPublicVisible() {
        when(viewProductsByShopRepository.findVisibleProductsByShopId(
                eq(shopId),
                eq(ProductDiscoverySort.NEWEST),
                any(),
                eq(now)
        )).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(new ViewProductsByShopCommand(shopId, 1, 20, null)))
                .isInstanceOf(AppException.class)
                .extracting(ex -> ((AppException) ex).getErrorCode())
                .isEqualTo(ErrorCode.SHOP_NOT_FOUND);
    }

    @Test
    void shouldRejectInvalidPagination() {
        assertThatThrownBy(() -> useCase.execute(new ViewProductsByShopCommand(shopId, 0, 20, null)))
                .isInstanceOf(AppException.class)
                .extracting(ex -> ((AppException) ex).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_PAGINATION);
    }

    private ViewProductsByShopResult sampleResult() {
        return new ViewProductsByShopResult(
                new PublicShopSummary(
                        shopId,
                        "Tech Shop",
                        "Best phones",
                        "http://localhost:9000/2hands-commerce-shop/avatar.jpg",
                        null,
                        BigDecimal.valueOf(4.5),
                        10,
                        false,
                        null,
                        UUID.randomUUID()
                ),
                List.of(new ProductCardSummary(
                        UUID.randomUUID(),
                        "iPhone 15",
                        "http://localhost:9000/2hands-commerce-product/p1.jpg",
                        shopId,
                        "Tech Shop",
                        UUID.randomUUID(),
                        "LIKE_NEW",
                        ProductStatus.ACTIVE,
                        BigDecimal.valueOf(20_000_000),
                        null,
                        BigDecimal.valueOf(20_000_000),
                        true,
                        false,
                        BigDecimal.valueOf(4.5),
                        10,
                        false,
                        null
                )),
                PageMeta.of(1, 20, 1)
        );
    }
}
