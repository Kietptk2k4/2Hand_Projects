package com.twohands.commerce_service.delivery.http.seller;

import com.twohands.commerce_service.application.product.archiveproduct.ArchiveProductCommand;
import com.twohands.commerce_service.application.product.archiveproduct.ArchiveProductResult;
import com.twohands.commerce_service.application.product.archiveproduct.ArchiveProductUseCase;
import com.twohands.commerce_service.application.product.pauseproduct.PauseProductCommand;
import com.twohands.commerce_service.application.product.pauseproduct.PauseProductResult;
import com.twohands.commerce_service.application.product.pauseproduct.PauseProductUseCase;
import com.twohands.commerce_service.application.product.publishproduct.PublishProductCommand;
import com.twohands.commerce_service.application.product.publishproduct.PublishProductResult;
import com.twohands.commerce_service.application.product.publishproduct.PublishProductUseCase;
import com.twohands.commerce_service.application.product.createproduct.CreateProductCommand;
import com.twohands.commerce_service.application.product.createproduct.CreateProductUseCase;
import com.twohands.commerce_service.application.product.updateproduct.UpdateProductCommand;
import com.twohands.commerce_service.application.product.updateproduct.UpdateProductUseCase;
import com.twohands.commerce_service.application.product.updateproductattributes.UpdateProductAttributesCommand;
import com.twohands.commerce_service.application.product.updateproductattributes.UpdateProductAttributesUseCase;
import com.twohands.commerce_service.application.product.updateproductmedia.UpdateProductMediaCommand;
import com.twohands.commerce_service.application.product.updateproductmedia.UpdateProductMediaUseCase;
import com.twohands.commerce_service.application.product.uploadproductmedia.CreateProductMediaUploadUrlCommand;
import com.twohands.commerce_service.application.product.uploadproductmedia.CreateProductMediaUploadUrlResult;
import com.twohands.commerce_service.application.product.uploadproductmedia.CreateProductMediaUploadUrlUseCase;
import com.twohands.commerce_service.application.product.updateproductinventory.UpdateProductInventoryCommand;
import com.twohands.commerce_service.application.product.updateproductinventory.UpdateProductInventoryUseCase;
import com.twohands.commerce_service.application.product.updateproductprice.UpdateProductPriceCommand;
import com.twohands.commerce_service.application.product.updateproductprice.UpdateProductPriceUseCase;
import com.twohands.commerce_service.application.product.viewsellerproductdetail.ViewSellerProductDetailCommand;
import com.twohands.commerce_service.application.product.viewsellerproductdetail.ViewSellerProductDetailUseCase;
import com.twohands.commerce_service.application.product.viewsellerproducts.ViewSellerProductsCommand;
import com.twohands.commerce_service.application.product.viewsellerproducts.ViewSellerProductsUseCase;
import com.twohands.commerce_service.common.dto.ApiResponse;
import com.twohands.commerce_service.common.pagination.PageMeta;
import com.twohands.commerce_service.delivery.http.catalog.PageMetaResponse;
import com.twohands.commerce_service.domain.product.SellerProductAttributeItem;
import com.twohands.commerce_service.domain.product.SellerProductDetail;
import com.twohands.commerce_service.domain.product.SellerProductListItem;
import com.twohands.commerce_service.domain.product.SellerProductListSummary;
import com.twohands.commerce_service.domain.product.ViewSellerProductsResult;
import com.twohands.commerce_service.domain.product.CreateProductResult;
import com.twohands.commerce_service.domain.product.ProductAttributeItem;
import com.twohands.commerce_service.domain.product.UpdateProductAttributesResult;
import com.twohands.commerce_service.domain.product.UpdateProductMediaResult;
import com.twohands.commerce_service.domain.product.UpdateProductInventoryResult;
import com.twohands.commerce_service.domain.product.UpdateProductPriceResult;
import com.twohands.commerce_service.domain.product.UpdateProductResult;
import jakarta.validation.Valid;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import com.twohands.commerce_service.security.AuthenticatedUser;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/commerce/api/v1/seller/products")
public class SellerProductController {

    private final CreateProductUseCase createProductUseCase;
    private final PublishProductUseCase publishProductUseCase;
    private final PauseProductUseCase pauseProductUseCase;
    private final ArchiveProductUseCase archiveProductUseCase;
    private final UpdateProductUseCase updateProductUseCase;
    private final UpdateProductAttributesUseCase updateProductAttributesUseCase;
    private final UpdateProductMediaUseCase updateProductMediaUseCase;
    private final CreateProductMediaUploadUrlUseCase createProductMediaUploadUrlUseCase;
    private final UpdateProductInventoryUseCase updateProductInventoryUseCase;
    private final UpdateProductPriceUseCase updateProductPriceUseCase;
    private final ViewSellerProductsUseCase viewSellerProductsUseCase;
    private final ViewSellerProductDetailUseCase viewSellerProductDetailUseCase;

    public SellerProductController(
            CreateProductUseCase createProductUseCase,
            PublishProductUseCase publishProductUseCase,
            PauseProductUseCase pauseProductUseCase,
            ArchiveProductUseCase archiveProductUseCase,
            UpdateProductUseCase updateProductUseCase,
            UpdateProductAttributesUseCase updateProductAttributesUseCase,
            UpdateProductMediaUseCase updateProductMediaUseCase,
            CreateProductMediaUploadUrlUseCase createProductMediaUploadUrlUseCase,
            UpdateProductInventoryUseCase updateProductInventoryUseCase,
            UpdateProductPriceUseCase updateProductPriceUseCase,
            ViewSellerProductsUseCase viewSellerProductsUseCase,
            ViewSellerProductDetailUseCase viewSellerProductDetailUseCase
    ) {
        this.createProductUseCase = createProductUseCase;
        this.publishProductUseCase = publishProductUseCase;
        this.pauseProductUseCase = pauseProductUseCase;
        this.archiveProductUseCase = archiveProductUseCase;
        this.updateProductUseCase = updateProductUseCase;
        this.updateProductAttributesUseCase = updateProductAttributesUseCase;
        this.updateProductMediaUseCase = updateProductMediaUseCase;
        this.createProductMediaUploadUrlUseCase = createProductMediaUploadUrlUseCase;
        this.updateProductInventoryUseCase = updateProductInventoryUseCase;
        this.updateProductPriceUseCase = updateProductPriceUseCase;
        this.viewSellerProductsUseCase = viewSellerProductsUseCase;
        this.viewSellerProductDetailUseCase = viewSellerProductDetailUseCase;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<ViewSellerProductsResponse>> viewSellerProducts(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer limit,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String q,
            Authentication authentication
    ) {
        UUID sellerId = resolveUserId(authentication);
        ViewSellerProductsResult result = viewSellerProductsUseCase.execute(
                new ViewSellerProductsCommand(sellerId, page, limit, status, q)
        );

        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK.value(),
                viewSellerProductsUseCase.successMessage(),
                toListResponse(result)
        ));
    }

    @GetMapping("/{productId}")
    public ResponseEntity<ApiResponse<ViewSellerProductDetailResponse>> viewSellerProductDetail(
            @PathVariable UUID productId,
            Authentication authentication
    ) {
        UUID sellerId = resolveUserId(authentication);
        SellerProductDetail detail = viewSellerProductDetailUseCase.execute(
                new ViewSellerProductDetailCommand(sellerId, productId)
        );

        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK.value(),
                viewSellerProductDetailUseCase.successMessage(),
                toDetailResponse(detail)
        ));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CreateProductResponse>> createProduct(
            @RequestBody @Valid CreateProductRequest request,
            Authentication authentication
    ) {
        UUID sellerId = resolveUserId(authentication);
        CreateProductResult result = createProductUseCase.execute(new CreateProductCommand(
                sellerId,
                request.productType(),
                request.categoryId(),
                request.brandId(),
                request.condition(),
                request.title(),
                request.description(),
                request.weightGram()
        ));

        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK.value(),
                createProductUseCase.successMessage(),
                toCreateResponse(result)
        ));
    }

    @PostMapping("/{productId}/media/upload-url")
    public ResponseEntity<ApiResponse<CreateShopMediaUploadUrlResponse>> createProductMediaUploadUrl(
            @PathVariable UUID productId,
            @RequestBody @Valid CreateShopMediaUploadUrlRequest request,
            Authentication authentication
    ) {
        UUID sellerId = resolveUserId(authentication);
        CreateProductMediaUploadUrlResult result = createProductMediaUploadUrlUseCase.execute(
                new CreateProductMediaUploadUrlCommand(
                        sellerId,
                        productId,
                        request.contentType(),
                        request.fileSizeBytes(),
                        request.mediaKind(),
                        request.clientUploadOrigin()
                )
        );

        CreateShopMediaUploadUrlResponse response = new CreateShopMediaUploadUrlResponse(
                result.uploadUrl(),
                result.objectKey(),
                result.mediaUrl(),
                result.mediaKind(),
                result.expiresAt(),
                result.maxFileSizeBytes(),
                result.allowedContentTypes()
        );

        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK.value(),
                createProductMediaUploadUrlUseCase.successMessage(),
                response
        ));
    }

    @PatchMapping("/{productId}/media")
    public ResponseEntity<ApiResponse<UpdateProductMediaResponse>> updateProductMedia(
            @PathVariable UUID productId,
            @RequestBody @Valid UpdateProductMediaRequest request,
            Authentication authentication
    ) {
        UUID sellerId = resolveUserId(authentication);
        UpdateProductMediaResult result = updateProductMediaUseCase.execute(
                new UpdateProductMediaCommand(sellerId, productId, request.mediaUrls())
        );

        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK.value(),
                updateProductMediaUseCase.successMessage(),
                toMediaResponse(result)
        ));
    }

    @PutMapping("/{productId}/attributes")
    public ResponseEntity<ApiResponse<UpdateProductAttributesResponse>> updateProductAttributes(
            @PathVariable UUID productId,
            @RequestBody @Valid UpdateProductAttributesRequest request,
            Authentication authentication
    ) {
        UUID sellerId = resolveUserId(authentication);
        UpdateProductAttributesResult result = updateProductAttributesUseCase.execute(
                new UpdateProductAttributesCommand(
                        sellerId,
                        productId,
                        request.attributes().stream()
                                .map(item -> new ProductAttributeItem(
                                        item.attributeName(),
                                        item.attributeValue()
                                ))
                                .toList()
                )
        );

        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK.value(),
                updateProductAttributesUseCase.successMessage(),
                toAttributesResponse(result)
        ));
    }

    @PostMapping("/{productId}/prices")
    public ResponseEntity<ApiResponse<UpdateProductPriceResponse>> updateProductPrice(
            @PathVariable UUID productId,
            @RequestBody @Valid UpdateProductPriceRequest request,
            Authentication authentication
    ) {
        UUID sellerId = resolveUserId(authentication);
        UpdateProductPriceResult result = updateProductPriceUseCase.execute(
                new UpdateProductPriceCommand(
                        sellerId,
                        productId,
                        request.price(),
                        request.salePrice(),
                        request.startAt(),
                        request.endAt()
                )
        );

        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK.value(),
                updateProductPriceUseCase.successMessage(),
                toPriceResponse(result)
        ));
    }

    @PatchMapping("/{productId}/inventory")
    public ResponseEntity<ApiResponse<UpdateProductInventoryResponse>> updateProductInventory(
            @PathVariable UUID productId,
            @RequestBody @Valid UpdateProductInventoryRequest request,
            Authentication authentication
    ) {
        UUID sellerId = resolveUserId(authentication);
        UpdateProductInventoryResult result = updateProductInventoryUseCase.execute(
                new UpdateProductInventoryCommand(
                        sellerId,
                        productId,
                        request.stockQuantity(),
                        request.lowStockThreshold()
                )
        );

        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK.value(),
                updateProductInventoryUseCase.successMessage(),
                toInventoryResponse(result)
        ));
    }

    @PatchMapping("/{productId}")
    public ResponseEntity<ApiResponse<UpdateProductResponse>> updateProduct(
            @PathVariable UUID productId,
            @RequestBody @Valid UpdateProductRequest request,
            Authentication authentication
    ) {
        UUID sellerId = resolveUserId(authentication);
        UpdateProductResult result = updateProductUseCase.execute(new UpdateProductCommand(
                sellerId,
                productId,
                request.productType(),
                request.categoryId(),
                request.brandId(),
                request.condition(),
                request.title(),
                request.description(),
                request.weightGram()
        ));

        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK.value(),
                updateProductUseCase.successMessage(),
                toUpdateResponse(result)
        ));
    }

    @PostMapping("/{productId}/publish")
    public ResponseEntity<ApiResponse<PublishProductResponse>> publishProduct(
            @PathVariable UUID productId,
            Authentication authentication
    ) {
        UUID sellerId = resolveUserId(authentication);
        PublishProductResult result = publishProductUseCase.execute(new PublishProductCommand(sellerId, productId));

        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK.value(),
                publishProductUseCase.successMessage(result.alreadyPublished()),
                toPublishResponse(result)
        ));
    }

    @PostMapping("/{productId}/pause")
    public ResponseEntity<ApiResponse<PauseProductResponse>> pauseProduct(
            @PathVariable UUID productId,
            Authentication authentication
    ) {
        UUID sellerId = resolveUserId(authentication);
        PauseProductResult result = pauseProductUseCase.execute(new PauseProductCommand(sellerId, productId));

        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK.value(),
                pauseProductUseCase.successMessage(result.alreadyPaused()),
                toPauseResponse(result)
        ));
    }

    @PostMapping("/{productId}/archive")
    public ResponseEntity<ApiResponse<ArchiveProductResponse>> archiveProduct(
            @PathVariable UUID productId,
            Authentication authentication
    ) {
        UUID sellerId = resolveUserId(authentication);
        ArchiveProductResult result = archiveProductUseCase.execute(new ArchiveProductCommand(sellerId, productId));

        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK.value(),
                archiveProductUseCase.successMessage(result.alreadyArchived()),
                toResponse(result)
        ));
    }

    private UUID resolveUserId(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedUser principal)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        return principal.userId();
    }

    private ViewSellerProductsResponse toListResponse(ViewSellerProductsResult result) {
        PageMeta pagination = result.pagination();
        SellerProductListSummary summary = result.summary();
        return new ViewSellerProductsResponse(
                result.items().stream().map(this::toListItemResponse).toList(),
                new PageMetaResponse(
                        pagination.page(),
                        pagination.limit(),
                        pagination.totalItems(),
                        pagination.totalPages(),
                        pagination.hasNext()
                ),
                new SellerProductListSummaryResponse(
                        summary.total(),
                        summary.active(),
                        summary.outOfStock(),
                        summary.draft(),
                        summary.paused(),
                        summary.archived(),
                        summary.lowStock()
                )
        );
    }

    private SellerProductListItemResponse toListItemResponse(SellerProductListItem item) {
        return new SellerProductListItemResponse(
                item.productId(),
                item.sellerId(),
                item.shopId(),
                item.status(),
                item.productType(),
                item.categoryId(),
                item.categoryName(),
                item.condition(),
                item.title(),
                item.description(),
                item.weightGram(),
                item.thumbnailUrl(),
                item.price(),
                item.salePrice(),
                item.effectivePrice(),
                item.stockQuantity(),
                item.lowStockThreshold(),
                item.createdAt(),
                item.updatedAt()
        );
    }

    private ViewSellerProductDetailResponse toDetailResponse(SellerProductDetail detail) {
        return new ViewSellerProductDetailResponse(
                detail.productId(),
                detail.sellerId(),
                detail.shopId(),
                detail.status(),
                detail.productType(),
                detail.categoryId(),
                detail.categoryName(),
                detail.brandId(),
                detail.condition(),
                detail.title(),
                detail.description(),
                detail.weightGram(),
                detail.thumbnailUrl(),
                detail.price(),
                detail.salePrice(),
                detail.effectivePrice(),
                detail.priceId(),
                detail.stockQuantity(),
                detail.lowStockThreshold(),
                detail.reservedQuantity(),
                detail.attributes().stream().map(this::toAttributeResponse).toList(),
                detail.mediaUrls(),
                detail.hasPrice(),
                detail.hasInventory(),
                detail.hasMedia(),
                detail.createdAt(),
                detail.updatedAt()
        );
    }

    private SellerProductAttributeResponse toAttributeResponse(SellerProductAttributeItem attribute) {
        return new SellerProductAttributeResponse(attribute.attributeName(), attribute.attributeValue());
    }

    private UpdateProductPriceResponse toPriceResponse(UpdateProductPriceResult result) {
        return new UpdateProductPriceResponse(
                result.productId(),
                result.sellerId(),
                result.shopId(),
                result.status(),
                result.price().priceId(),
                result.price().price(),
                result.price().salePrice(),
                result.price().effectivePrice(),
                result.price().startAt(),
                result.price().endAt(),
                result.price().createdAt(),
                result.previousActivePriceClosed()
        );
    }

    private UpdateProductInventoryResponse toInventoryResponse(UpdateProductInventoryResult result) {
        return new UpdateProductInventoryResponse(
                result.productId(),
                result.sellerId(),
                result.shopId(),
                result.status(),
                result.previousStatus(),
                result.statusChanged(),
                result.stockQuantity(),
                result.lowStockThreshold(),
                result.reservedQuantity(),
                result.cartItemsSynced(),
                result.updatedAt()
        );
    }

    private UpdateProductMediaResponse toMediaResponse(UpdateProductMediaResult result) {
        return new UpdateProductMediaResponse(
                result.productId(),
                result.sellerId(),
                result.shopId(),
                result.status(),
                result.thumbnailUrl(),
                result.mediaUrls(),
                result.hasMedia()
        );
    }

    private UpdateProductAttributesResponse toAttributesResponse(UpdateProductAttributesResult result) {
        return new UpdateProductAttributesResponse(
                result.productId(),
                result.sellerId(),
                result.shopId(),
                result.status(),
                result.attributes().stream()
                        .map(item -> new UpdateProductAttributesResponse.AttributeItemResponse(
                                item.attributeName(),
                                item.attributeValue()
                        ))
                        .toList()
        );
    }

    private UpdateProductResponse toUpdateResponse(UpdateProductResult result) {
        return new UpdateProductResponse(
                result.productId(),
                result.sellerId(),
                result.shopId(),
                result.status(),
                result.productType(),
                result.categoryId(),
                result.brandId(),
                result.condition(),
                result.title(),
                result.description(),
                result.weightGram(),
                result.createdAt(),
                result.updatedAt()
        );
    }

    private CreateProductResponse toCreateResponse(CreateProductResult result) {
        return new CreateProductResponse(
                result.productId(),
                result.sellerId(),
                result.shopId(),
                result.status(),
                result.productType(),
                result.categoryId(),
                result.brandId(),
                result.condition(),
                result.title(),
                result.description(),
                result.weightGram(),
                result.createdAt(),
                result.updatedAt()
        );
    }

    private PublishProductResponse toPublishResponse(PublishProductResult result) {
        return new PublishProductResponse(
                result.productId(),
                result.shopId(),
                result.status(),
                result.publishedAt(),
                result.alreadyPublished()
        );
    }

    private PauseProductResponse toPauseResponse(PauseProductResult result) {
        return new PauseProductResponse(
                result.productId(),
                result.shopId(),
                result.status(),
                result.pausedAt(),
                result.cartItemsInvalidated(),
                result.alreadyPaused()
        );
    }

    private ArchiveProductResponse toResponse(ArchiveProductResult result) {
        return new ArchiveProductResponse(
                result.productId(),
                result.shopId(),
                result.status(),
                result.archivedAt(),
                result.cartItemsInvalidated(),
                result.alreadyArchived()
        );
    }
}
