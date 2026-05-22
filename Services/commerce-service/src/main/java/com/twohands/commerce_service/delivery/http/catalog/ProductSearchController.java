package com.twohands.commerce_service.delivery.http.catalog;

import com.twohands.commerce_service.application.product.searchproduct.SearchProductCommand;
import com.twohands.commerce_service.application.product.searchproduct.SearchProductUseCase;
import com.twohands.commerce_service.common.dto.ApiResponse;
import com.twohands.commerce_service.common.pagination.PageMeta;
import com.twohands.commerce_service.domain.discovery.ProductCardSummary;
import com.twohands.commerce_service.domain.discovery.SearchProductResult;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/commerce/api/v1/products")
public class ProductSearchController {

    private final SearchProductUseCase searchProductUseCase;

    public ProductSearchController(SearchProductUseCase searchProductUseCase) {
        this.searchProductUseCase = searchProductUseCase;
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<SearchProductResponse>> searchProducts(
            @RequestParam("q") String keyword,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer limit,
            @RequestParam(required = false) String sort
    ) {
        SearchProductResult result = searchProductUseCase.execute(
                new SearchProductCommand(keyword, page, limit, sort)
        );

        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK.value(),
                searchProductUseCase.successMessage(),
                toResponse(result)
        ));
    }

    private SearchProductResponse toResponse(SearchProductResult result) {
        PageMeta pagination = result.pagination();
        return new SearchProductResponse(
                result.keyword(),
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
