package com.twohands.commerce_service.delivery.http.catalog;

import com.twohands.commerce_service.application.product.filterproductsbycategory.FilterProductsByCategoryCommand;
import com.twohands.commerce_service.application.product.filterproductsbycategory.FilterProductsByCategoryUseCase;
import com.twohands.commerce_service.common.dto.ApiResponse;
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

    public CategoryProductController(FilterProductsByCategoryUseCase filterProductsByCategoryUseCase) {
        this.filterProductsByCategoryUseCase = filterProductsByCategoryUseCase;
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
