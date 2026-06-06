package com.twohands.commerce_service.unit.application.product;

import com.twohands.commerce_service.application.product.viewsellerproducts.ViewSellerProductsCommand;
import com.twohands.commerce_service.application.product.viewsellerproducts.ViewSellerProductsUseCase;
import com.twohands.commerce_service.common.pagination.PageMeta;
import com.twohands.commerce_service.domain.product.ProductStatus;
import com.twohands.commerce_service.domain.product.SellerProductListItem;
import com.twohands.commerce_service.domain.product.SellerProductListSummary;
import com.twohands.commerce_service.domain.product.ViewSellerProductCatalogRepository;
import com.twohands.commerce_service.domain.product.ViewSellerProductsResult;
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
class ViewSellerProductsUseCaseTest {

    private static final Instant NOW = Instant.parse("2026-06-06T07:00:00Z");

    @Mock
    private SellerShopRepository sellerShopRepository;

    @Mock
    private ViewSellerProductCatalogRepository viewSellerProductCatalogRepository;

    private ViewSellerProductsUseCase useCase;

    private final UUID sellerId = UUID.randomUUID();
    private final UUID shopId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        useCase = new ViewSellerProductsUseCase(
                sellerShopRepository,
                viewSellerProductCatalogRepository,
                Clock.fixed(NOW, ZoneOffset.UTC)
        );
    }

    @Test
    void executeShouldReturnPagedProductsAndSummary() {
        when(sellerShopRepository.findBySellerId(sellerId)).thenReturn(Optional.of(sampleShop()));
        when(viewSellerProductCatalogRepository.countBySellerId(eq(sellerId), any(), any())).thenReturn(1L);
        when(viewSellerProductCatalogRepository.findBySellerId(eq(sellerId), any(), any(), any(), eq(NOW)))
                .thenReturn(List.of(sampleItem()));
        when(viewSellerProductCatalogRepository.summarizeBySellerId(sellerId))
                .thenReturn(new SellerProductListSummary(1, 1, 0, 0, 0, 0, 0));

        ViewSellerProductsResult result = useCase.execute(
                new ViewSellerProductsCommand(sellerId, 1, 10, null, null)
        );

        assertThat(result.items()).hasSize(1);
        assertThat(result.pagination()).isEqualTo(PageMeta.of(1, 10, 1));
        assertThat(result.summary().active()).isEqualTo(1);
    }

    @Test
    void executeShouldFailWhenSellerHasNoShop() {
        when(sellerShopRepository.findBySellerId(sellerId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(new ViewSellerProductsCommand(sellerId, 1, 10, null, null)))
                .isInstanceOf(AppException.class)
                .extracting(ex -> ((AppException) ex).getErrorCode())
                .isEqualTo(ErrorCode.SELLER_SHOP_NOT_FOUND);

        verify(viewSellerProductCatalogRepository, never()).findBySellerId(any(), any(), any(), any(), any());
    }

    @Test
    void executeShouldRejectInvalidStatusFilter() {
        when(sellerShopRepository.findBySellerId(sellerId)).thenReturn(Optional.of(sampleShop()));

        assertThatThrownBy(() -> useCase.execute(new ViewSellerProductsCommand(sellerId, 1, 10, "BAD", null)))
                .isInstanceOf(AppException.class)
                .extracting(ex -> ((AppException) ex).getErrorCode())
                .isEqualTo(ErrorCode.VALIDATION_ERROR);
    }

    private SellerShop sampleShop() {
        return new SellerShop(shopId, sellerId, ShopStatus.ACTIVE);
    }

    private SellerProductListItem sampleItem() {
        UUID productId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();
        return new SellerProductListItem(
                productId,
                sellerId,
                shopId,
                ProductStatus.ACTIVE,
                "PHYSICAL",
                categoryId,
                "Tools",
                "NEW",
                "Hammer",
                "Heavy hammer",
                500,
                "https://cdn.example/hammer.jpg",
                BigDecimal.valueOf(100_000),
                null,
                BigDecimal.valueOf(100_000),
                5,
                3,
                NOW,
                NOW
        );
    }
}
