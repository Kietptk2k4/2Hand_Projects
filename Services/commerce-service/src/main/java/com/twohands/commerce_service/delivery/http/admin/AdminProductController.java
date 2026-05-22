package com.twohands.commerce_service.delivery.http.admin;

import com.twohands.commerce_service.application.product.removeproductbyadmin.RemoveProductByAdminCommand;
import com.twohands.commerce_service.application.product.removeproductbyadmin.RemoveProductByAdminUseCase;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/commerce/api/v1/admin/products")
public class AdminProductController {

    private final RemoveProductByAdminUseCase removeProductByAdminUseCase;
    private final CommerceAdminAuthorization commerceAdminAuthorization;

    public AdminProductController(
            RemoveProductByAdminUseCase removeProductByAdminUseCase,
            CommerceAdminAuthorization commerceAdminAuthorization
    ) {
        this.removeProductByAdminUseCase = removeProductByAdminUseCase;
        this.commerceAdminAuthorization = commerceAdminAuthorization;
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
}
