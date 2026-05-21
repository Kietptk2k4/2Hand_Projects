package com.twohands.commerce_service.unit.application.product;

import com.twohands.commerce_service.application.product.filterproductsbycategory.FilterProductsByCategoryCommand;
import com.twohands.commerce_service.application.product.filterproductsbycategory.FilterProductsByCategoryUseCase;
import com.twohands.commerce_service.common.pagination.PageMeta;
import com.twohands.commerce_service.domain.catalog.ActiveCategory;
import com.twohands.commerce_service.domain.catalog.CategoryReadRepository;
import com.twohands.commerce_service.domain.discovery.FilterProductsByCategoryResult;
import com.twohands.commerce_service.domain.discovery.ProductCardSummary;
import com.twohands.commerce_service.domain.discovery.ProductDiscoveryRepository;
import com.twohands.commerce_service.domain.discovery.ProductDiscoverySort;
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
class FilterProductsByCategoryUseCaseTest {

    @Mock
    private CategoryReadRepository categoryReadRepository;

    @Mock
    private ProductDiscoveryRepository productDiscoveryRepository;

    private FilterProductsByCategoryUseCase useCase;

    private final UUID categoryId = UUID.randomUUID();
    private final UUID childCategoryId = UUID.randomUUID();
    private final Instant now = Instant.parse("2026-05-21T16:00:00Z");

    @BeforeEach
    void setUp() {
        useCase = new FilterProductsByCategoryUseCase(
                categoryReadRepository,
                productDiscoveryRepository,
                Clock.fixed(now, ZoneOffset.UTC)
        );
    }

    @Test
    void shouldFilterProductsIncludingChildCategoriesByDefault() {
        ActiveCategory category = new ActiveCategory(categoryId, "Electronics", "electronics", "/" + categoryId + "/");
        when(categoryReadRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(categoryReadRepository.resolveCategoryIdsForFilter(categoryId, category.path(), true))
                .thenReturn(List.of(categoryId, childCategoryId));
        when(productDiscoveryRepository.countVisibleProductsByCategories(List.of(categoryId, childCategoryId), now))
                .thenReturn(1L);
        when(productDiscoveryRepository.findVisibleProductsByCategories(
                eq(List.of(categoryId, childCategoryId)),
                eq(ProductDiscoverySort.NEWEST),
                any(),
                eq(now)
        )).thenReturn(List.of(sampleCard()));

        FilterProductsByCategoryResult result = useCase.execute(
                new FilterProductsByCategoryCommand(categoryId, 1, 20, null, null)
        );

        assertThat(result.includeChildren()).isTrue();
        assertThat(result.items()).hasSize(1);
        assertThat(result.pagination().totalItems()).isEqualTo(1);
    }

    @Test
    void shouldFilterOnlyExactCategoryWhenIncludeChildrenFalse() {
        ActiveCategory category = new ActiveCategory(categoryId, "Phones", "phones", "/" + categoryId + "/");
        when(categoryReadRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(categoryReadRepository.resolveCategoryIdsForFilter(categoryId, category.path(), false))
                .thenReturn(List.of(categoryId));
        when(productDiscoveryRepository.countVisibleProductsByCategories(List.of(categoryId), now)).thenReturn(1L);

        when(productDiscoveryRepository.findVisibleProductsByCategories(
                eq(List.of(categoryId)),
                eq(ProductDiscoverySort.PRICE_ASC),
                any(),
                eq(now)
        )).thenReturn(List.of());

        FilterProductsByCategoryResult result = useCase.execute(
                new FilterProductsByCategoryCommand(categoryId, 1, 10, "PRICE_ASC", false)
        );

        assertThat(result.includeChildren()).isFalse();
        assertThat(result.items()).isEmpty();
        verify(productDiscoveryRepository).findVisibleProductsByCategories(
                eq(List.of(categoryId)),
                eq(ProductDiscoverySort.PRICE_ASC),
                any(),
                eq(now)
        );
    }

    @Test
    void shouldRejectInactiveOrMissingCategory() {
        when(categoryReadRepository.findById(categoryId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(
                new FilterProductsByCategoryCommand(categoryId, 1, 20, null, true)))
                .isInstanceOf(AppException.class)
                .extracting(ex -> ((AppException) ex).getErrorCode())
                .isEqualTo(ErrorCode.CATEGORY_NOT_FOUND);
    }

    @Test
    void shouldRejectInvalidPagination() {
        assertThatThrownBy(() -> useCase.execute(
                new FilterProductsByCategoryCommand(categoryId, 0, 20, null, true)))
                .isInstanceOf(AppException.class)
                .extracting(ex -> ((AppException) ex).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_PAGINATION);
    }

    @Test
    void shouldRejectInvalidSort() {
        assertThatThrownBy(() -> useCase.execute(
                new FilterProductsByCategoryCommand(categoryId, 1, 20, "invalid", true)))
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
                categoryId,
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
