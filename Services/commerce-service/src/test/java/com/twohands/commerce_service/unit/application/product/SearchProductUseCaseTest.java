package com.twohands.commerce_service.unit.application.product;

import com.twohands.commerce_service.application.product.searchproduct.SearchProductCommand;
import com.twohands.commerce_service.application.product.searchproduct.SearchProductUseCase;
import com.twohands.commerce_service.domain.discovery.ProductCardSummary;
import com.twohands.commerce_service.domain.discovery.ProductDiscoveryRepository;
import com.twohands.commerce_service.domain.discovery.ProductDiscoverySort;
import com.twohands.commerce_service.domain.discovery.SearchProductResult;
import com.twohands.commerce_service.domain.product.ProductStatus;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
class SearchProductUseCaseTest {

    @Mock
    private ProductDiscoveryRepository productDiscoveryRepository;

    private SearchProductUseCase useCase;

    private final Instant now = Instant.parse("2026-05-21T16:00:00Z");

    @BeforeEach
    void setUp() {
        useCase = new SearchProductUseCase(
                productDiscoveryRepository,
                Clock.fixed(now, ZoneOffset.UTC)
        );
    }

    @Test
    void shouldSearchVisibleProductsByKeyword() {
        when(productDiscoveryRepository.countVisibleProductsByKeyword(any(), eq(now))).thenReturn(1L);
        when(productDiscoveryRepository.findVisibleProductsByKeyword(
                any(),
                eq(ProductDiscoverySort.NEWEST),
                any(),
                eq(now)
        )).thenReturn(List.of(sampleCard()));

        SearchProductResult result = useCase.execute(new SearchProductCommand("  iphone   15 ", 1, 20, null));

        assertThat(result.keyword()).isEqualTo("iphone 15");
        assertThat(result.items()).hasSize(1);
        assertThat(result.pagination().totalItems()).isEqualTo(1);

        ArgumentCaptor<String> patternCaptor = ArgumentCaptor.forClass(String.class);
        verify(productDiscoveryRepository).countVisibleProductsByKeyword(patternCaptor.capture(), eq(now));
        assertThat(patternCaptor.getValue()).isEqualTo("%iphone 15%");
    }

    @Test
    void shouldEscapeLikeWildcardsInKeyword() {
        when(productDiscoveryRepository.countVisibleProductsByKeyword(any(), eq(now))).thenReturn(0L);

        useCase.execute(new SearchProductCommand("100% off", 1, 20, null));

        ArgumentCaptor<String> patternCaptor = ArgumentCaptor.forClass(String.class);
        verify(productDiscoveryRepository).countVisibleProductsByKeyword(patternCaptor.capture(), eq(now));
        assertThat(patternCaptor.getValue()).isEqualTo("%100\\% off%");
    }

    @Test
    void shouldApplyPriceDescSort() {
        when(productDiscoveryRepository.countVisibleProductsByKeyword(any(), eq(now))).thenReturn(1L);
        when(productDiscoveryRepository.findVisibleProductsByKeyword(
                any(),
                eq(ProductDiscoverySort.PRICE_DESC),
                any(),
                eq(now)
        )).thenReturn(List.of());

        useCase.execute(new SearchProductCommand("laptop", 1, 10, "PRICE_DESC"));

        verify(productDiscoveryRepository).findVisibleProductsByKeyword(
                any(),
                eq(ProductDiscoverySort.PRICE_DESC),
                any(),
                eq(now)
        );
    }

    @Test
    void shouldRejectEmptyKeyword() {
        assertThatThrownBy(() -> useCase.execute(new SearchProductCommand("  ", 1, 20, null)))
                .isInstanceOf(AppException.class)
                .extracting(ex -> ((AppException) ex).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_SEARCH_KEYWORD);
    }

    @Test
    void shouldRejectTooShortKeyword() {
        assertThatThrownBy(() -> useCase.execute(new SearchProductCommand("a", 1, 20, null)))
                .isInstanceOf(AppException.class)
                .extracting(ex -> ((AppException) ex).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_SEARCH_KEYWORD);
    }

    @Test
    void shouldRejectTooLongKeyword() {
        String keyword = "x".repeat(256);
        assertThatThrownBy(() -> useCase.execute(new SearchProductCommand(keyword, 1, 20, null)))
                .isInstanceOf(AppException.class)
                .extracting(ex -> ((AppException) ex).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_SEARCH_KEYWORD);
    }

    @Test
    void shouldRejectInvalidPagination() {
        assertThatThrownBy(() -> useCase.execute(new SearchProductCommand("phone", 0, 20, null)))
                .isInstanceOf(AppException.class)
                .extracting(ex -> ((AppException) ex).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_PAGINATION);
    }

    @Test
    void shouldRejectInvalidSort() {
        assertThatThrownBy(() -> useCase.execute(new SearchProductCommand("phone", 1, 20, "invalid")))
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
                "USED",
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
