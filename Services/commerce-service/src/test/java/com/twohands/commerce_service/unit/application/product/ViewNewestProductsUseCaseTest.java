package com.twohands.commerce_service.unit.application.product;

import com.twohands.commerce_service.application.product.viewnewestproducts.ViewNewestProductsCommand;
import com.twohands.commerce_service.application.product.viewnewestproducts.ViewNewestProductsUseCase;
import com.twohands.commerce_service.common.pagination.PageQuery;
import com.twohands.commerce_service.domain.discovery.ProductCardSummary;
import com.twohands.commerce_service.domain.discovery.ProductDiscoveryRepository;
import com.twohands.commerce_service.domain.discovery.ProductDiscoverySort;
import com.twohands.commerce_service.domain.discovery.ViewProductListResult;
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
class ViewNewestProductsUseCaseTest {

    private static final Instant NOW = Instant.parse("2026-08-02T09:30:00Z");

    @Mock
    private ProductDiscoveryRepository productDiscoveryRepository;

    private ViewNewestProductsUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new ViewNewestProductsUseCase(
                productDiscoveryRepository,
                Clock.fixed(NOW, ZoneOffset.UTC)
        );
    }

    @Test
    void executeShouldReturnNewestProductsWithPagination() {
        ProductCardSummary card = sampleCard();
        when(productDiscoveryRepository.countAllVisibleProducts(NOW)).thenReturn(1L);
        when(productDiscoveryRepository.findAllVisibleProducts(
                eq(ProductDiscoverySort.NEWEST), any(PageQuery.class), eq(NOW)
        )).thenReturn(List.of(card));

        ViewProductListResult result = useCase.execute(new ViewNewestProductsCommand(null, null, null));

        assertThat(result.items()).hasSize(1);
        assertThat(result.pagination().totalItems()).isEqualTo(1);
        assertThat(result.pagination().hasNext()).isFalse();
        verify(productDiscoveryRepository).findAllVisibleProducts(
                eq(ProductDiscoverySort.NEWEST), any(PageQuery.class), eq(NOW)
        );
    }

    @Test
    void executeShouldRejectInvalidPage() {
        assertThatThrownBy(() -> useCase.execute(new ViewNewestProductsCommand(0, 20, null)))
                .isInstanceOf(AppException.class)
                .extracting(ex -> ((AppException) ex).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_PAGINATION);
    }

    private ProductCardSummary sampleCard() {
        return new ProductCardSummary(
                UUID.randomUUID(),
                "Ao Nike",
                "http://localhost/thumb.jpg",
                UUID.randomUUID(),
                "Shop",
                UUID.randomUUID(),
                "GOOD",
                ProductStatus.ACTIVE,
                BigDecimal.valueOf(200_000),
                null,
                BigDecimal.valueOf(200_000),
                null,
                null,
                true,
                false,
                BigDecimal.valueOf(4.5),
                3,
                false,
                null
        );
    }
}
