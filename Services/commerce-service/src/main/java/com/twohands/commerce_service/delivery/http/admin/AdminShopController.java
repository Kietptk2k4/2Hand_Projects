package com.twohands.commerce_service.delivery.http.admin;

import com.twohands.commerce_service.application.admin.viewadminshops.ViewAdminShopsForModerationCommand;
import com.twohands.commerce_service.application.admin.viewadminshops.ViewAdminShopsForModerationUseCase;
import com.twohands.commerce_service.application.shop.moderateshop.ModerateShopCommand;
import com.twohands.commerce_service.application.shop.moderateshop.ModerateShopUseCase;
import com.twohands.commerce_service.domain.admin.ViewAdminShopsForModerationResult;
import com.twohands.commerce_service.common.dto.ApiResponse;
import com.twohands.commerce_service.domain.shop.ModerateShopResult;
import com.twohands.commerce_service.domain.shop.ShopModerationAction;
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
@RequestMapping("/commerce/api/v1/admin/shops")
public class AdminShopController {

    private final ViewAdminShopsForModerationUseCase viewAdminShopsForModerationUseCase;
    private final ModerateShopUseCase moderateShopUseCase;
    private final CommerceAdminAuthorization commerceAdminAuthorization;

    public AdminShopController(
            ViewAdminShopsForModerationUseCase viewAdminShopsForModerationUseCase,
            ModerateShopUseCase moderateShopUseCase,
            CommerceAdminAuthorization commerceAdminAuthorization
    ) {
        this.viewAdminShopsForModerationUseCase = viewAdminShopsForModerationUseCase;
        this.moderateShopUseCase = moderateShopUseCase;
        this.commerceAdminAuthorization = commerceAdminAuthorization;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<ViewAdminShopsForModerationResponse>> listShops(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer limit,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String sort,
            Authentication authentication
    ) {
        AuthenticatedUser admin = resolveAuthenticatedUser(authentication);
        commerceAdminAuthorization.requireAnyPermission(
                admin,
                CommerceAdminAuthorization.PERMISSION_SHOP_SUSPEND,
                CommerceAdminAuthorization.PERMISSION_SHOP_CLOSE
        );

        ViewAdminShopsForModerationResult result = viewAdminShopsForModerationUseCase.execute(
                new ViewAdminShopsForModerationCommand(page, limit, status, q, sort)
        );

        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK.value(),
                viewAdminShopsForModerationUseCase.successMessage(),
                AdminModerationListResponseMapper.toShopListResponse(result)
        ));
    }

    @PostMapping("/{shopId}/moderate")
    public ResponseEntity<ApiResponse<ModerateShopResponse>> moderateShop(
            @PathVariable UUID shopId,
            @RequestBody @Valid ModerateShopRequest request,
            Authentication authentication
    ) {
        AuthenticatedUser admin = resolveAuthenticatedUser(authentication);
        ShopModerationAction action = parseAction(request.action());
        requirePermissionForAction(admin, action);

        ModerateShopResult result = moderateShopUseCase.execute(new ModerateShopCommand(
                admin.userId(),
                shopId,
                action,
                request.reason()
        ));

        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK.value(),
                moderateShopUseCase.successMessage(action, result.alreadyModerated()),
                toResponse(result)
        ));
    }

    private void requirePermissionForAction(AuthenticatedUser admin, ShopModerationAction action) {
        switch (action) {
            case SUSPEND -> commerceAdminAuthorization.requirePermission(
                    admin, CommerceAdminAuthorization.PERMISSION_SHOP_SUSPEND
            );
            case CLOSE -> commerceAdminAuthorization.requirePermission(
                    admin, CommerceAdminAuthorization.PERMISSION_SHOP_CLOSE
            );
            case RESTORE -> commerceAdminAuthorization.requireAnyPermission(
                    admin,
                    CommerceAdminAuthorization.PERMISSION_SHOP_SUSPEND,
                    CommerceAdminAuthorization.PERMISSION_SHOP_CLOSE
            );
        }
    }

    private ShopModerationAction parseAction(String raw) {
        try {
            return ShopModerationAction.valueOf(raw.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new AppException(ErrorCode.INVALID_SHOP_MODERATION, "Invalid shop moderation action: " + raw);
        }
    }

    private AuthenticatedUser resolveAuthenticatedUser(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedUser principal)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        return principal;
    }

    private ModerateShopResponse toResponse(ModerateShopResult result) {
        return new ModerateShopResponse(
                result.shopId(),
                result.sellerId(),
                result.shopName(),
                result.status(),
                result.previousStatus(),
                result.alreadyModerated(),
                result.cartItemsInvalidated(),
                result.moderatedAt()
        );
    }
}
