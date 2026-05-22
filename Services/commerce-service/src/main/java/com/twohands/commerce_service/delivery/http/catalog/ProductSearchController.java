package com.twohands.commerce_service.delivery.http.catalog;

import com.twohands.commerce_service.application.product.searchproduct.SearchProductCommand;
import com.twohands.commerce_service.application.product.searchproduct.SearchProductUseCase;
import com.twohands.commerce_service.application.product.viewproductdetail.ViewProductDetailCommand;
import com.twohands.commerce_service.application.product.viewproductdetail.ViewProductDetailUseCase;
import com.twohands.commerce_service.application.product.viewproductlist.ViewProductListCommand;
import com.twohands.commerce_service.application.product.viewproductlist.ViewProductListUseCase;
import com.twohands.commerce_service.application.review.viewproductreviews.ViewProductReviewsCommand;
import com.twohands.commerce_service.application.review.viewproductreviews.ViewProductReviewsUseCase;
import com.twohands.commerce_service.common.dto.ApiResponse;
import com.twohands.commerce_service.common.pagination.PageMeta;
import com.twohands.commerce_service.domain.discovery.ProductCardSummary;
import com.twohands.commerce_service.domain.discovery.SearchProductResult;
import com.twohands.commerce_service.domain.discovery.ViewProductListResult;
import com.twohands.commerce_service.domain.product.ViewProductDetailAttributeItem;
import com.twohands.commerce_service.domain.product.ViewProductDetailMediaItem;
import com.twohands.commerce_service.domain.product.ViewProductDetailResult;
import com.twohands.commerce_service.domain.review.ProductReviewListItem;
import com.twohands.commerce_service.domain.review.ProductReviewSellerReply;
import com.twohands.commerce_service.domain.review.ReviewMediaItem;
import com.twohands.commerce_service.domain.review.ViewProductReviewsResult;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/commerce/api/v1/products")
public class ProductSearchController {

    private final ViewProductListUseCase viewProductListUseCase;
    private final SearchProductUseCase searchProductUseCase;
    private final ViewProductDetailUseCase viewProductDetailUseCase;
    private final ViewProductReviewsUseCase viewProductReviewsUseCase;

    public ProductSearchController(
            ViewProductListUseCase viewProductListUseCase,
            SearchProductUseCase searchProductUseCase,
            ViewProductDetailUseCase viewProductDetailUseCase,
            ViewProductReviewsUseCase viewProductReviewsUseCase
    ) {
        this.viewProductListUseCase = viewProductListUseCase;
        this.searchProductUseCase = searchProductUseCase;
        this.viewProductDetailUseCase = viewProductDetailUseCase;
        this.viewProductReviewsUseCase = viewProductReviewsUseCase;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<ViewProductListResponse>> listProducts(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer limit,
            @RequestParam(required = false) String sort
    ) {
        ViewProductListResult result = viewProductListUseCase.execute(
                new ViewProductListCommand(page, limit, sort)
        );

        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK.value(),
                viewProductListUseCase.successMessage(),
                toListResponse(result)
        ));
    }

    @GetMapping("/{productId}/reviews")
    public ResponseEntity<ApiResponse<ViewProductReviewsResponse>> viewProductReviews(
            @PathVariable UUID productId,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer limit,
            @RequestParam(required = false) Integer rating,
            @RequestParam(required = false) String sort
    ) {
        ViewProductReviewsResult result = viewProductReviewsUseCase.execute(
                new ViewProductReviewsCommand(productId, page, limit, rating, sort)
        );

        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK.value(),
                viewProductReviewsUseCase.successMessage(),
                toReviewsResponse(result)
        ));
    }

    @GetMapping("/{productId}")
    public ResponseEntity<ApiResponse<ViewProductDetailResponse>> viewProductDetail(
            @PathVariable UUID productId
    ) {
        ViewProductDetailResult result = viewProductDetailUseCase.execute(
                new ViewProductDetailCommand(productId)
        );

        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK.value(),
                viewProductDetailUseCase.successMessage(),
                toDetailResponse(result)
        ));
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

    private ViewProductReviewsResponse toReviewsResponse(ViewProductReviewsResult result) {
        PageMeta pagination = result.pagination();
        return new ViewProductReviewsResponse(
                result.productId(),
                new ProductReviewRatingSummaryResponse(
                        result.ratingSummary().ratingAvg(),
                        result.ratingSummary().ratingCount()
                ),
                result.reviews().stream().map(this::toReviewItemResponse).toList(),
                new PageMetaResponse(
                        pagination.page(),
                        pagination.limit(),
                        pagination.totalItems(),
                        pagination.totalPages(),
                        pagination.hasNext()
                )
        );
    }

    private ProductReviewItemResponse toReviewItemResponse(ProductReviewListItem item) {
        return new ProductReviewItemResponse(
                item.reviewId(),
                item.rating(),
                item.comment(),
                item.createdAt(),
                item.media().stream().map(this::toReviewMediaResponse).toList(),
                item.sellerReply() == null ? null : toSellerReplyResponse(item.sellerReply())
        );
    }

    private ProductReviewMediaResponse toReviewMediaResponse(ReviewMediaItem media) {
        return new ProductReviewMediaResponse(
                media.id(),
                media.url(),
                media.type()
        );
    }

    private ProductReviewSellerReplyResponse toSellerReplyResponse(ProductReviewSellerReply reply) {
        return new ProductReviewSellerReplyResponse(
                reply.replyId(),
                reply.content(),
                reply.createdAt()
        );
    }

    private ViewProductListResponse toListResponse(ViewProductListResult result) {
        PageMeta pagination = result.pagination();
        return new ViewProductListResponse(
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

    private ViewProductDetailResponse toDetailResponse(ViewProductDetailResult result) {
        return new ViewProductDetailResponse(
                result.productId(),
                result.title(),
                result.description(),
                result.condition(),
                result.weightGram(),
                result.status(),
                new ViewProductDetailCategoryResponse(
                        result.category().categoryId(),
                        result.category().name(),
                        result.category().slug()
                ),
                new ViewProductDetailShopResponse(
                        result.shop().shopId(),
                        result.shop().shopName(),
                        result.shop().avatarUrl(),
                        result.shop().coverUrl()
                ),
                result.media().stream().map(this::toMediaResponse).toList(),
                result.attributes().stream().map(this::toAttributeResponse).toList(),
                result.price(),
                result.salePrice(),
                result.effectivePrice(),
                new ViewProductDetailInventorySummaryResponse(
                        result.inventorySummary().stockQuantity(),
                        result.inventorySummary().lowStockThreshold(),
                        result.inventorySummary().inStock(),
                        result.inventorySummary().lowStock()
                ),
                result.ratingAvg(),
                result.ratingCount(),
                result.shopVacation(),
                result.vacationMessage()
        );
    }

    private ViewProductDetailMediaResponse toMediaResponse(ViewProductDetailMediaItem item) {
        return new ViewProductDetailMediaResponse(
                item.mediaId(),
                item.mediaUrl(),
                item.mediaType(),
                item.sortOrder()
        );
    }

    private ViewProductDetailAttributeResponse toAttributeResponse(ViewProductDetailAttributeItem item) {
        return new ViewProductDetailAttributeResponse(
                item.attributeName(),
                item.attributeValue()
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
