package com.twohands.commerce_service.delivery.http.shop;

import com.twohands.commerce_service.application.product.viewproductsbyshop.ViewProductsByShopCommand;
import com.twohands.commerce_service.application.product.viewproductsbyshop.ViewProductsByShopUseCase;
import com.twohands.commerce_service.common.dto.ApiResponse;
import com.twohands.commerce_service.common.pagination.PageMeta;
import com.twohands.commerce_service.delivery.http.catalog.PageMetaResponse;
import com.twohands.commerce_service.delivery.http.catalog.ProductCardResponse;
import com.twohands.commerce_service.domain.discovery.ProductCardSummary;
import com.twohands.commerce_service.domain.discovery.PublicShopSummary;
import com.twohands.commerce_service.domain.discovery.ViewProductsByShopResult;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/commerce/api/v1/shops")
public class ShopProductController {

    private final ViewProductsByShopUseCase viewProductsByShopUseCase;

    public ShopProductController(ViewProductsByShopUseCase viewProductsByShopUseCase) {
        this.viewProductsByShopUseCase = viewProductsByShopUseCase;
    }

    @GetMapping("/{shopId}/products")
    public ResponseEntity<ApiResponse<ViewProductsByShopResponse>> viewProductsByShop(
            @PathVariable UUID shopId,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer limit,
            @RequestParam(required = false) String sort
    ) {
        ViewProductsByShopResult result = viewProductsByShopUseCase.execute(
                new ViewProductsByShopCommand(shopId, page, limit, sort)
        );

        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK.value(),
                viewProductsByShopUseCase.successMessage(),
                toResponse(result)
        ));
    }

    private ViewProductsByShopResponse toResponse(ViewProductsByShopResult result) {
        PageMeta pagination = result.pagination();
        return new ViewProductsByShopResponse(
                toShopResponse(result.shop()),
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

    private PublicShopSummaryResponse toShopResponse(PublicShopSummary shop) {
        return new PublicShopSummaryResponse(
                shop.shopId(),
                shop.shopName(),
                shop.description(),
                shop.avatarUrl(),
                shop.coverUrl(),
                shop.ratingAvg(),
                shop.ratingCount(),
                shop.shopVacation(),
                shop.vacationMessage()
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
