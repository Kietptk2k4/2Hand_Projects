package com.twohands.commerce_service.delivery.http.internal;

import com.twohands.commerce_service.application.moderation.lookup.LookupProductModerationOwnerUseCase;
import com.twohands.commerce_service.application.moderation.lookup.LookupReviewModerationPartiesUseCase;
import com.twohands.commerce_service.application.moderation.lookup.LookupShopModerationOwnerUseCase;
import com.twohands.commerce_service.application.product.removeproductbyadmin.RemoveProductByAdminCommand;
import com.twohands.commerce_service.application.product.removeproductbyadmin.RemoveProductByAdminUseCase;
import com.twohands.commerce_service.application.product.restoreproductbyadmin.RestoreProductByAdminCommand;
import com.twohands.commerce_service.application.product.restoreproductbyadmin.RestoreProductByAdminUseCase;
import com.twohands.commerce_service.application.review.moderatereview.ModerateReviewCommand;
import com.twohands.commerce_service.application.review.moderatereview.ModerateReviewUseCase;
import com.twohands.commerce_service.domain.review.ModerateReviewResult;
import com.twohands.commerce_service.domain.review.ReviewModerationAction;
import com.twohands.commerce_service.common.dto.ApiResponse;
import com.twohands.commerce_service.domain.moderation.ProductModerationOwner;
import com.twohands.commerce_service.domain.moderation.ReviewModerationParties;
import com.twohands.commerce_service.domain.moderation.ShopModerationOwner;
import com.twohands.commerce_service.domain.product.RemoveProductByAdminResult;
import com.twohands.commerce_service.domain.product.RestoreProductByAdminResult;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/commerce/api/v1/internal/moderation")
public class CommerceInternalModerationController {

    private final LookupProductModerationOwnerUseCase lookupProductModerationOwnerUseCase;
    private final LookupShopModerationOwnerUseCase lookupShopModerationOwnerUseCase;
    private final LookupReviewModerationPartiesUseCase lookupReviewModerationPartiesUseCase;
    private final RemoveProductByAdminUseCase removeProductByAdminUseCase;
    private final RestoreProductByAdminUseCase restoreProductByAdminUseCase;
    private final ModerateReviewUseCase moderateReviewUseCase;

    public CommerceInternalModerationController(
            LookupProductModerationOwnerUseCase lookupProductModerationOwnerUseCase,
            LookupShopModerationOwnerUseCase lookupShopModerationOwnerUseCase,
            LookupReviewModerationPartiesUseCase lookupReviewModerationPartiesUseCase,
            RemoveProductByAdminUseCase removeProductByAdminUseCase,
            RestoreProductByAdminUseCase restoreProductByAdminUseCase,
            ModerateReviewUseCase moderateReviewUseCase
    ) {
        this.lookupProductModerationOwnerUseCase = lookupProductModerationOwnerUseCase;
        this.lookupShopModerationOwnerUseCase = lookupShopModerationOwnerUseCase;
        this.lookupReviewModerationPartiesUseCase = lookupReviewModerationPartiesUseCase;
        this.removeProductByAdminUseCase = removeProductByAdminUseCase;
        this.restoreProductByAdminUseCase = restoreProductByAdminUseCase;
        this.moderateReviewUseCase = moderateReviewUseCase;
    }

    @PostMapping("/products/{productId}/remove")
    public ResponseEntity<ApiResponse<InternalRemoveProductResponse>> removeProduct(
            @PathVariable UUID productId,
            @RequestBody @Valid InternalRemoveProductRequest request
    ) {
        RemoveProductByAdminResult result = removeProductByAdminUseCase.execute(
                new RemoveProductByAdminCommand(request.removedByAdminId(), productId, request.reason())
        );
        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK.value(),
                removeProductByAdminUseCase.successMessage(result.alreadyRemoved()),
                InternalRemoveProductResponse.from(result)
        ));
    }

    @PostMapping("/products/{productId}/restore")
    public ResponseEntity<ApiResponse<InternalRestoreProductResponse>> restoreProduct(
            @PathVariable UUID productId,
            @RequestBody @Valid InternalRestoreProductRequest request
    ) {
        RestoreProductByAdminResult result = restoreProductByAdminUseCase.execute(
                new RestoreProductByAdminCommand(request.restoredByAdminId(), productId, request.reason())
        );
        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK.value(),
                restoreProductByAdminUseCase.successMessage(result.alreadyRestored()),
                InternalRestoreProductResponse.from(result)
        ));
    }

    @GetMapping("/products/{productId}")
    public ResponseEntity<ApiResponse<ProductModerationOwnerResponse>> lookupProductOwner(
            @PathVariable UUID productId
    ) {
        ProductModerationOwner owner = lookupProductModerationOwnerUseCase.execute(productId);
        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK.value(),
                "Product moderation owner resolved",
                ProductModerationOwnerResponse.from(owner)
        ));
    }

    @GetMapping("/shops/{shopId}")
    public ResponseEntity<ApiResponse<ShopModerationOwnerResponse>> lookupShopOwner(
            @PathVariable UUID shopId
    ) {
        ShopModerationOwner owner = lookupShopModerationOwnerUseCase.execute(shopId);
        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK.value(),
                "Shop moderation owner resolved",
                ShopModerationOwnerResponse.from(owner)
        ));
    }

    @PostMapping("/reviews/{reviewId}/moderate")
    public ResponseEntity<ApiResponse<InternalModerateReviewResponse>> moderateReview(
            @PathVariable UUID reviewId,
            @RequestBody @Valid InternalModerateReviewRequest request
    ) {
        ReviewModerationAction action = ReviewModerationAction.valueOf(request.action().trim().toUpperCase());
        ModerateReviewResult result = moderateReviewUseCase.execute(new ModerateReviewCommand(
                request.moderatedByAdminId(),
                reviewId,
                action,
                request.reason()
        ));
        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK.value(),
                moderateReviewUseCase.successMessage(action, result.alreadyModerated()),
                InternalModerateReviewResponse.from(result)
        ));
    }

    @GetMapping("/reviews/{reviewId}")
    public ResponseEntity<ApiResponse<ReviewModerationPartiesResponse>> lookupReviewParties(
            @PathVariable UUID reviewId
    ) {
        ReviewModerationParties parties = lookupReviewModerationPartiesUseCase.execute(reviewId);
        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK.value(),
                "Review moderation parties resolved",
                ReviewModerationPartiesResponse.from(parties)
        ));
    }
}
