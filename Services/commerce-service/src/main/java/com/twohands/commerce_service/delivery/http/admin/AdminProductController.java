package com.twohands.commerce_service.delivery.http.admin;

import com.twohands.commerce_service.application.admin.viewadminproducts.ViewAdminProductsForModerationCommand;
import com.twohands.commerce_service.application.admin.viewadminproducts.ViewAdminProductsForModerationUseCase;
import com.twohands.commerce_service.application.admin.viewproductdetailformoderation.ViewProductDetailForModerationCommand;
import com.twohands.commerce_service.application.admin.viewproductdetailformoderation.ViewProductDetailForModerationResult;
import com.twohands.commerce_service.application.admin.viewproductdetailformoderation.ViewProductDetailForModerationUseCase;
import com.twohands.commerce_service.application.product.removeproductbyadmin.RemoveProductByAdminCommand;
import com.twohands.commerce_service.application.product.removeproductbyadmin.RemoveProductByAdminUseCase;
import com.twohands.commerce_service.domain.admin.ViewAdminProductsForModerationResult;
import com.twohands.commerce_service.common.dto.ApiResponse;
import com.twohands.commerce_service.domain.product.RemoveProductByAdminResult;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import com.twohands.commerce_service.security.AuthenticatedUser;
import com.twohands.commerce_service.security.CommerceAdminAuthorization;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/commerce/api/v1/admin/products")
public class AdminProductController {

    private final ViewAdminProductsForModerationUseCase viewAdminProductsForModerationUseCase;
    private final ViewProductDetailForModerationUseCase viewProductDetailForModerationUseCase;
    private final RemoveProductByAdminUseCase removeProductByAdminUseCase;
    private final CommerceAdminAuthorization commerceAdminAuthorization;

    public AdminProductController(
            ViewAdminProductsForModerationUseCase viewAdminProductsForModerationUseCase,
            ViewProductDetailForModerationUseCase viewProductDetailForModerationUseCase,
            RemoveProductByAdminUseCase removeProductByAdminUseCase,
            CommerceAdminAuthorization commerceAdminAuthorization
    ) {
        this.viewAdminProductsForModerationUseCase = viewAdminProductsForModerationUseCase;
        this.viewProductDetailForModerationUseCase = viewProductDetailForModerationUseCase;
        this.removeProductByAdminUseCase = removeProductByAdminUseCase;
        this.commerceAdminAuthorization = commerceAdminAuthorization;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<ViewAdminProductsForModerationResponse>> listProducts(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer limit,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String sort,
            Authentication authentication
    ) {
        AuthenticatedUser admin = resolveAuthenticatedUser(authentication);
        commerceAdminAuthorization.requirePermission(
                admin,
                CommerceAdminAuthorization.PERMISSION_PRODUCT_REMOVE
        );

        ViewAdminProductsForModerationResult result = viewAdminProductsForModerationUseCase.execute(
                new ViewAdminProductsForModerationCommand(page, limit, status, q, sort)
        );

        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK.value(),
                viewAdminProductsForModerationUseCase.successMessage(),
                AdminModerationListResponseMapper.toProductListResponse(result)
        ));
    }

    @GetMapping("/{productId}")
    public ResponseEntity<ApiResponse<AdminProductDetailResponse>> getProductDetail(
            @PathVariable UUID productId,
            Authentication authentication
    ) {
        AuthenticatedUser admin = resolveAuthenticatedUser(authentication);
        commerceAdminAuthorization.requirePermission(
                admin,
                CommerceAdminAuthorization.PERMISSION_PRODUCT_REMOVE
        );

        ViewProductDetailForModerationResult result = viewProductDetailForModerationUseCase.execute(
                new ViewProductDetailForModerationCommand(productId)
        );

        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK.value(),
                viewProductDetailForModerationUseCase.successMessage(),
                toDetailResponse(result)
        ));
    }

    @PostMapping("/{productId}/remove")
    public ResponseEntity<ApiResponse<RemoveProductByAdminResponse>> removeProduct(
            @PathVariable UUID productId,
            @RequestBody @Valid RemoveProductByAdminRequest request,
            Authentication authentication
    ) {
        AuthenticatedUser admin = resolveAuthenticatedUser(authentication);
        commerceAdminAuthorization.requirePermission(
                admin,
                CommerceAdminAuthorization.PERMISSION_PRODUCT_REMOVE
        );

        RemoveProductByAdminResult result = removeProductByAdminUseCase.execute(
                new RemoveProductByAdminCommand(admin.userId(), productId, request.reason())
        );

        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK.value(),
                removeProductByAdminUseCase.successMessage(result.alreadyRemoved()),
                toResponse(result)
        ));
    }

    private AuthenticatedUser resolveAuthenticatedUser(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedUser principal)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        return principal;
    }

    private RemoveProductByAdminResponse toResponse(RemoveProductByAdminResult result) {
        return new RemoveProductByAdminResponse(
                result.productId(),
                result.sellerId(),
                result.shopId(),
                result.title(),
                result.status(),
                result.previousStatus(),
                result.alreadyRemoved(),
                result.cartItemsInvalidated(),
                result.removedAt()
        );
    }

    private AdminProductDetailResponse toDetailResponse(ViewProductDetailForModerationResult result) {
        return new AdminProductDetailResponse(
                result.productId(),
                result.sellerId(),
                result.shopId(),
                result.shopName(),
                result.title(),
                result.description(),
                result.status(),
                result.categoryId(),
                result.categoryName(),
                result.price(),
                result.effectivePrice(),
                result.stockQuantity(),
                result.createdAt(),
                result.updatedAt(),
                result.removedAt(),
                result.removeReason(),
                result.openOrderCount(),
                result.media().stream()
                        .map(item -> new AdminProductDetailMediaResponse(
                                item.mediaUrl(),
                                item.mediaType(),
                                item.sortOrder()
                        ))
                        .toList(),
                result.attributes().stream()
                        .map(item -> new AdminProductDetailAttributeResponse(
                                item.name(),
                                item.value()
                        ))
                        .toList()
        );
    }
}
