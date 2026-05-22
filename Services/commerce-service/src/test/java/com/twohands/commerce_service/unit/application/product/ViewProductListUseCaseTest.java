package com.twohands.commerce_service.unit.application.product;

import com.twohands.commerce_service.application.product.viewproductlist.ViewProductListCommand;
import com.twohands.commerce_service.application.product.viewproductlist.ViewProductListUseCase;
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
class ViewProductListUseCaseTest {

    @Mock
    private ProductDiscoveryRepository productDiscoveryRepository;

    private ViewProductListUseCase useCase;

    private final Instant now = Instant.parse("2026-05-21T16:00:00Z");

    @BeforeEach
    void setUp() {
        useCase = new ViewProductListUseCase(
                productDiscoveryRepository,
                Clock.fixed(now, ZoneOffset.UTC)
        );
    }

    @Test
    void shouldReturnVisibleProductListWithPagination() {
        when(productDiscoveryRepository.countAllVisibleProducts(now)).thenReturn(2L);
        when(productDiscoveryRepository.findAllVisibleProducts(
                eq(ProductDiscoverySort.PRICE_ASC),
                any(),
                eq(now)
        )).thenReturn(List.of(sampleCard(), sampleCard()));

        ViewProductListResult result = useCase.execute(new ViewProductListCommand(1, 20, "price_asc"));

        assertThat(result.items()).hasSize(2);
        assertThat(result.pagination().page()).isEqualTo(1);
        assertThat(result.pagination().limit()).isEqualTo(20);
        assertThat(result.pagination().totalItems()).isEqualTo(2);
        assertThat(result.pagination().hasNext()).isFalse();

        verify(productDiscoveryRepository).findAllVisibleProducts(
                eq(ProductDiscoverySort.PRICE_ASC),
                any(),
                eq(now)
        );
    }

    @Test
    void shouldReturnEmptyListWhenNoVisibleProducts() {
        when(productDiscoveryRepository.countAllVisibleProducts(now)).thenReturn(0L);

        ViewProductListResult result = useCase.execute(new ViewProductListCommand(null, null, null));

        assertThat(result.items()).isEmpty();
        assertThat(result.pagination().totalItems()).isZero();
    }

    @Test
    void shouldRejectInvalidPagination() {
        assertThatThrownBy(() -> useCase.execute(new ViewProductListCommand(0, 20, null)))
                .isInstanceOf(AppException.class)
                .extracting(ex -> ((AppException) ex).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_PAGINATION);
    }

    @Test
    void shouldRejectInvalidSort() {
        assertThatThrownBy(() -> useCase.execute(new ViewProductListCommand(1, 20, "invalid")))
                .isInstanceOf(AppException.class)
                .extracting(ex -> ((AppException) ex).getErrorCode())
                .isEqualTo(ErrorCode.VALIDATION_ERROR);
    }

    private ProductCardSummary sampleCard() {
        return new ProductCardSummary(
                UUID.randomUUID(),
                "iPhone 15",
                "http://localhost:9000/2hands-commerce-product/p1.jpg",
                UUID.randomUUID(),
                "Tech Shop",
                UUID.randomUUID(),
                "LIKE_NEW",
                ProductStatus.ACTIVE,
                BigDecimal.valueOf(20_000_000),
                BigDecimal.valueOf(18_000_000),
                BigDecimal.valueOf(18_000_000),
                true,
                false,
                BigDecimal.valueOf(4.5),
                10,
                false,
                null
        );
    }
}
