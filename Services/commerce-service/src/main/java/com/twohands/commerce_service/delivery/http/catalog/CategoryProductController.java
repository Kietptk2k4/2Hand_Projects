package com.twohands.commerce_service.delivery.http.catalog;

import com.twohands.commerce_service.application.catalog.viewactivecategories.ViewActiveCategoriesCommand;
import com.twohands.commerce_service.application.catalog.viewactivecategories.ViewActiveCategoriesUseCase;
import com.twohands.commerce_service.application.product.filterproductsbycategory.FilterProductsByCategoryCommand;
import com.twohands.commerce_service.application.product.filterproductsbycategory.FilterProductsByCategoryUseCase;
import com.twohands.commerce_service.common.dto.ApiResponse;
import com.twohands.commerce_service.domain.catalog.CategorySummary;
import com.twohands.commerce_service.common.pagination.PageMeta;
import com.twohands.commerce_service.domain.discovery.FilterProductsByCategoryResult;
import com.twohands.commerce_service.domain.discovery.ProductCardSummary;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/commerce/api/v1/categories")
public class CategoryProductController {

    private final FilterProductsByCategoryUseCase filterProductsByCategoryUseCase;
    private final ViewActiveCategoriesUseCase viewActiveCategoriesUseCase;

    public CategoryProductController(
            FilterProductsByCategoryUseCase filterProductsByCategoryUseCase,
            ViewActiveCategoriesUseCase viewActiveCategoriesUseCase
    ) {
        this.filterProductsByCategoryUseCase = filterProductsByCategoryUseCase;
        this.viewActiveCategoriesUseCase = viewActiveCategoriesUseCase;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<ViewActiveCategoriesResponse>> listActiveCategories(
            @RequestParam(name = "min_level", required = false) Integer minLevel,
            @RequestParam(name = "max_level", required = false) Integer maxLevel,
            @RequestParam(name = "leaf_only", required = false) Boolean leafOnly,
            @RequestParam(name = "include_product_counts", required = false) Boolean includeProductCounts
    ) {
        List<CategorySummary> items = viewActiveCategoriesUseCase.execute(
                new ViewActiveCategoriesCommand(minLevel, maxLevel, leafOnly, includeProductCounts)
        );

        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK.value(),
                viewActiveCategoriesUseCase.successMessage(),
                toCategoriesResponse(items)
        ));
    }

    @GetMapping("/{categoryId}/products")
    public ResponseEntity<ApiResponse<FilterProductsByCategoryResponse>> filterProductsByCategory(
            @PathVariable UUID categoryId,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer limit,
            @RequestParam(required = false) String sort,
            @RequestParam(name = "include_children", required = false) Boolean includeChildren
    ) {
        FilterProductsByCategoryResult result = filterProductsByCategoryUseCase.execute(
                new FilterProductsByCategoryCommand(categoryId, page, limit, sort, includeChildren)
        );

        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK.value(),
                filterProductsByCategoryUseCase.successMessage(),
                toResponse(result)
        ));
    }

    private ViewActiveCategoriesResponse toCategoriesResponse(List<CategorySummary> items) {
        return new ViewActiveCategoriesResponse(items.stream().map(this::toCategorySummary).toList());
    }

    private ViewActiveCategoriesResponse.CategorySummaryResponse toCategorySummary(CategorySummary item) {
        return new ViewActiveCategoriesResponse.CategorySummaryResponse(
                item.id(),
                item.name(),
                item.slug(),
                item.parentId(),
                item.level(),
                item.leaf(),
                item.productCount()
        );
    }

    private FilterProductsByCategoryResponse toResponse(FilterProductsByCategoryResult result) {
        PageMeta pagination = result.pagination();
        return new FilterProductsByCategoryResponse(
                result.category().id(),
                result.category().name(),
                result.category().slug(),
                result.includeChildren(),
                result.items().stream().map(this::toProductCard).toList(),
                new PageMetaResponse(
                        pagination.page(),
                        pagination.limit(),
                        pagination.totalItems(),
                        pagination.totalPages(),
                        pagination.hasNext()
                )
        );
    }

    private ProductCardResponse toProductCard(ProductCardSummary item) {
        return new ProductCardResponse(
                item.productId(),
                item.title(),
                item.thumbnailUrl(),
                item.shopId(),
                item.shopName(),
                item.categoryId(),
                item.condition(),
                item.status(),
                item.price(),
                item.salePrice(),
                item.effectivePrice(),
                item.inStock(),
                item.lowStock(),
                item.ratingAvg(),
                item.ratingCount(),
                item.shopVacation(),
                item.vacationMessage()
        );
    }
}
