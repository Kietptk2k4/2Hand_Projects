package com.twohands.commerce_service.delivery.http.seller;

import com.twohands.commerce_service.application.product.archiveproduct.ArchiveProductCommand;
import com.twohands.commerce_service.application.product.archiveproduct.ArchiveProductResult;
import com.twohands.commerce_service.application.product.archiveproduct.ArchiveProductUseCase;
import com.twohands.commerce_service.common.dto.ApiResponse;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import com.twohands.commerce_service.security.AuthenticatedUser;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/commerce/api/v1/seller/products")
public class SellerProductController {

    private final ArchiveProductUseCase archiveProductUseCase;

    public SellerProductController(ArchiveProductUseCase archiveProductUseCase) {
        this.archiveProductUseCase = archiveProductUseCase;
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
