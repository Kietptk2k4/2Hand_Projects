package com.twohands.commerce_service.unit.application.product;

import com.twohands.commerce_service.application.product.viewsellerproductdetail.ViewSellerProductDetailCommand;
import com.twohands.commerce_service.application.product.viewsellerproductdetail.ViewSellerProductDetailUseCase;
import com.twohands.commerce_service.domain.product.ProductStatus;
import com.twohands.commerce_service.domain.product.SellerProductDetail;
import com.twohands.commerce_service.domain.product.ViewSellerProductCatalogRepository;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ViewSellerProductDetailUseCaseTest {

    private static final Instant NOW = Instant.parse("2026-06-06T07:00:00Z");

    @Mock
    private SellerShopRepository sellerShopRepository;

    @Mock
    private ViewSellerProductCatalogRepository viewSellerProductCatalogRepository;

    private ViewSellerProductDetailUseCase useCase;

    private final UUID sellerId = UUID.randomUUID();
    private final UUID shopId = UUID.randomUUID();
    private final UUID productId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        useCase = new ViewSellerProductDetailUseCase(
                sellerShopRepository,
                viewSellerProductCatalogRepository,
                Clock.fixed(NOW, ZoneOffset.UTC)
        );
    }

    @Test
    void executeShouldReturnOwnedProductDetail() {
        when(sellerShopRepository.findBySellerId(sellerId)).thenReturn(Optional.of(sampleShop()));
        when(viewSellerProductCatalogRepository.findDetailBySellerId(sellerId, productId, NOW))
                .thenReturn(Optional.of(sampleDetail()));

        SellerProductDetail detail = useCase.execute(new ViewSellerProductDetailCommand(sellerId, productId));

        assertThat(detail.productId()).isEqualTo(productId);
        assertThat(detail.hasPrice()).isTrue();
    }

    @Test
    void executeShouldFailWhenProductNotFound() {
        when(sellerShopRepository.findBySellerId(sellerId)).thenReturn(Optional.of(sampleShop()));
        when(viewSellerProductCatalogRepository.findDetailBySellerId(eq(sellerId), eq(productId), eq(NOW)))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(new ViewSellerProductDetailCommand(sellerId, productId)))
                .isInstanceOf(AppException.class)
                .extracting(ex -> ((AppException) ex).getErrorCode())
                .isEqualTo(ErrorCode.PRODUCT_NOT_FOUND);
    }

    private SellerShop sampleShop() {
        return new SellerShop(shopId, sellerId, ShopStatus.ACTIVE);
    }

    private SellerProductDetail sampleDetail() {
        return new SellerProductDetail(
                productId,
                sellerId,
                shopId,
                ProductStatus.DRAFT,
                "PHYSICAL",
                UUID.randomUUID(),
                "Tools",
                null,
                "NEW",
                "Hammer",
                "Heavy hammer",
                500,
                null,
                BigDecimal.valueOf(100_000),
                null,
                BigDecimal.valueOf(100_000),
                UUID.randomUUID(),
                5,
                3,
                0,
                List.of(),
                List.of(),
                true,
                true,
                false,
                NOW,
                NOW
        );
    }
}
