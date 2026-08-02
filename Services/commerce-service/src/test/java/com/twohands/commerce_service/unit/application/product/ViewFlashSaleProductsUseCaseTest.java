package com.twohands.commerce_service.unit.application.product;

import com.twohands.commerce_service.application.product.viewflashsaleproducts.ViewFlashSaleProductsCommand;
import com.twohands.commerce_service.application.product.viewflashsaleproducts.ViewFlashSaleProductsResult;
import com.twohands.commerce_service.application.product.viewflashsaleproducts.ViewFlashSaleProductsUseCase;
import com.twohands.commerce_service.domain.discovery.ProductCardSummary;
import com.twohands.commerce_service.domain.discovery.ProductDiscoveryRepository;
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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ViewFlashSaleProductsUseCaseTest {

    private static final Instant NOW = Instant.parse("2026-08-02T09:30:00Z");

    @Mock
    private ProductDiscoveryRepository productDiscoveryRepository;

    private ViewFlashSaleProductsUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new ViewFlashSaleProductsUseCase(
                productDiscoveryRepository,
                Clock.fixed(NOW, ZoneOffset.UTC)
        );
    }

    @Test
    void executeShouldReturnFlashSaleItemsWithinCurrentSlot() {
        ProductCardSummary card = sampleCard();
        when(productDiscoveryRepository.findFlashSaleProducts(eq(NOW), any(), any(), eq(20)))
                .thenReturn(List.of(card));

        ViewFlashSaleProductsResult result = useCase.execute(new ViewFlashSaleProductsCommand(null));

        assertThat(result.items()).hasSize(1);
        assertThat(result.slotStart()).isNotNull();
        assertThat(result.slotEnd()).isAfter(result.slotStart());
        verify(productDiscoveryRepository).findFlashSaleProducts(eq(NOW), any(), any(), eq(20));
    }

    @Test
    void executeShouldRejectInvalidLimit() {
        assertThatThrownBy(() -> useCase.execute(new ViewFlashSaleProductsCommand(0)))
                .isInstanceOf(AppException.class)
                .extracting(ex -> ((AppException) ex).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_PAGINATION);
    }

    private ProductCardSummary sampleCard() {
        return new ProductCardSummary(
                UUID.randomUUID(),
                "Nike Dunk",
                "http://localhost/thumb.jpg",
                UUID.randomUUID(),
                "Shop",
                UUID.randomUUID(),
                "GOOD",
                ProductStatus.ACTIVE,
                BigDecimal.valueOf(800_000),
                BigDecimal.valueOf(680_000),
                BigDecimal.valueOf(680_000),
                NOW.minusSeconds(300),
                NOW.plusSeconds(3600),
                true,
                false,
                BigDecimal.valueOf(4.8),
                10,
                false,
                null
        );
    }
}
