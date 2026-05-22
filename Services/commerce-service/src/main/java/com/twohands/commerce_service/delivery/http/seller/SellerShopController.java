package com.twohands.commerce_service.delivery.http.seller;

import com.twohands.commerce_service.application.shop.createshop.CreateShopCommand;
import com.twohands.commerce_service.application.shop.createshop.CreateShopUseCase;
import com.twohands.commerce_service.application.shop.updateshopprofile.UpdateShopProfileCommand;
import com.twohands.commerce_service.application.shop.updateshopprofile.UpdateShopProfileUseCase;
import com.twohands.commerce_service.common.dto.ApiResponse;
import com.twohands.commerce_service.domain.shop.CreateShopPickupDraft;
import com.twohands.commerce_service.domain.shop.CreateShopResult;
import com.twohands.commerce_service.domain.shop.UpdateShopProfileResult;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import com.twohands.commerce_service.security.AuthenticatedUser;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/commerce/api/v1/seller/shop")
public class SellerShopController {

    private final CreateShopUseCase createShopUseCase;
    private final UpdateShopProfileUseCase updateShopProfileUseCase;

    public SellerShopController(
            CreateShopUseCase createShopUseCase,
            UpdateShopProfileUseCase updateShopProfileUseCase
    ) {
        this.createShopUseCase = createShopUseCase;
        this.updateShopProfileUseCase = updateShopProfileUseCase;
    }

    @PatchMapping
    public ResponseEntity<ApiResponse<UpdateShopProfileResponse>> updateShopProfile(
            @RequestBody @Valid UpdateShopProfileRequest request,
            Authentication authentication
    ) {
        UUID sellerId = resolveUserId(authentication);
        UpdateShopProfileResult result = updateShopProfileUseCase.execute(
                new UpdateShopProfileCommand(
                        sellerId,
                        request.shopName(),
                        request.description(),
                        request.avatarUrl(),
                        request.coverUrl()
                )
        );

        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK.value(),
                updateShopProfileUseCase.successMessage(),
                toUpdateResponse(result)
        ));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CreateShopResponse>> createShop(
            @RequestBody @Valid CreateShopRequest request,
            Authentication authentication
    ) {
        UUID sellerId = resolveUserId(authentication);
        CreateShopResult result = createShopUseCase.execute(new CreateShopCommand(
                sellerId,
                request.shopName(),
                request.description(),
                request.avatarUrl(),
                request.coverUrl(),
                toPickupDraft(request.pickupProfile())
        ));

        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK.value(),
                createShopUseCase.successMessage(),
                toResponse(result)
        ));
    }

    private UUID resolveUserId(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedUser principal)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        return principal.userId();
    }

    private CreateShopPickupDraft toPickupDraft(CreateShopPickupRequest pickup) {
        if (pickup == null || !pickup.hasAnyField()) {
            return null;
        }
        return new CreateShopPickupDraft(
                pickup.pickupName(),
                pickup.phone(),
                pickup.provinceCode(),
                pickup.districtCode(),
                pickup.wardCode(),
                pickup.addressDetail()
        );
    }

    private UpdateShopProfileResponse toUpdateResponse(UpdateShopProfileResult result) {
        return new UpdateShopProfileResponse(
                result.shopId(),
                result.sellerId(),
                result.shopName(),
                result.description(),
                result.avatarUrl(),
                result.coverUrl(),
                result.status(),
                result.ratingAvg(),
                result.ratingCount(),
                result.vacationMode(),
                result.createdAt(),
                result.updatedAt()
        );
    }

    private CreateShopResponse toResponse(CreateShopResult result) {
        return new CreateShopResponse(
                result.shopId(),
                result.sellerId(),
                result.shopName(),
                result.description(),
                result.avatarUrl(),
                result.coverUrl(),
                result.status(),
                result.vacationMode(),
                result.shippingProfileCreated(),
                result.createdAt(),
                result.updatedAt()
        );
    }
}
