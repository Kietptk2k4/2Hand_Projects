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
import com.twohands.commerce_service.common.dto.ApiResponse;
import com.twohands.commerce_service.domain.product.CreateProductResult;
import jakarta.validation.Valid;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import com.twohands.commerce_service.security.AuthenticatedUser;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/commerce/api/v1/seller/products")
public class SellerProductController {

    private final CreateProductUseCase createProductUseCase;
    private final PublishProductUseCase publishProductUseCase;
    private final PauseProductUseCase pauseProductUseCase;
    private final ArchiveProductUseCase archiveProductUseCase;

    public SellerProductController(
            CreateProductUseCase createProductUseCase,
            PublishProductUseCase publishProductUseCase,
            PauseProductUseCase pauseProductUseCase,
            ArchiveProductUseCase archiveProductUseCase
    ) {
        this.createProductUseCase = createProductUseCase;
        this.publishProductUseCase = publishProductUseCase;
        this.pauseProductUseCase = pauseProductUseCase;
        this.archiveProductUseCase = archiveProductUseCase;
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
