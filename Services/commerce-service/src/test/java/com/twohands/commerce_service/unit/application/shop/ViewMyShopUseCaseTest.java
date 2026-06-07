package com.twohands.commerce_service.unit.application.shop;

import com.twohands.commerce_service.application.shop.viewmyshop.ViewMyShopCommand;
import com.twohands.commerce_service.application.shop.viewmyshop.ViewMyShopUseCase;
import com.twohands.commerce_service.domain.shop.ShopStatus;
import com.twohands.commerce_service.domain.shop.ViewMyShopRepository;
import com.twohands.commerce_service.domain.shop.ViewMyShopResult;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
class ViewMyShopUseCaseTest {

    @Mock
    private ViewMyShopRepository viewMyShopRepository;

    private ViewMyShopUseCase useCase;

    private final UUID sellerId = UUID.randomUUID();
    private final UUID shopId = UUID.randomUUID();
    private final Instant createdAt = Instant.parse("2026-05-20T10:00:00Z");
    private final Instant updatedAt = Instant.parse("2026-05-21T10:00:00Z");

    @BeforeEach
    void setUp() {
        useCase = new ViewMyShopUseCase(viewMyShopRepository);
    }

    @Test
    void shouldReturnShopForSeller() {
        when(viewMyShopRepository.findBySellerId(sellerId))
                .thenReturn(Optional.of(sampleResult()));

        ViewMyShopResult result = useCase.execute(new ViewMyShopCommand(sellerId));

        assertThat(result.shopId()).isEqualTo(shopId);
        assertThat(result.sellerId()).isEqualTo(sellerId);
        assertThat(result.shopName()).isEqualTo("Vintage Closet");
        assertThat(result.vacationMode()).isTrue();
        assertThat(result.vacationMessage()).isEqualTo("Shop nghi den 25/05");
    }

    @Test
    void shouldThrowWhenSellerHasNoShop() {
        when(viewMyShopRepository.findBySellerId(sellerId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(new ViewMyShopCommand(sellerId)))
                .isInstanceOf(AppException.class)
                .extracting(ex -> ((AppException) ex).getErrorCode())
                .isEqualTo(ErrorCode.SHOP_NOT_FOUND);
    }

    private ViewMyShopResult sampleResult() {
        return new ViewMyShopResult(
                shopId,
                sellerId,
                "Vintage Closet",
                "Do secondhand thoi trang",
                "https://minio.example/avatar.png",
                "https://minio.example/cover.png",
                ShopStatus.ACTIVE,
                new BigDecimal("4.50"),
                2,
                true,
                "Shop nghi den 25/05",
                createdAt,
                updatedAt
        );
    }
}