package com.twohands.commerce_service.delivery.http.shop;

import com.twohands.commerce_service.application.shop.viewpublicshopbyuser.ViewPublicShopByUserCommand;
import com.twohands.commerce_service.application.shop.viewpublicshopbyuser.ViewPublicShopByUserUseCase;
import com.twohands.commerce_service.common.dto.ApiResponse;
import com.twohands.commerce_service.domain.shop.PublicShopByUserSnapshot;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/commerce/api/v1/users")
public class PublicShopByUserController {

    private final ViewPublicShopByUserUseCase viewPublicShopByUserUseCase;

    public PublicShopByUserController(ViewPublicShopByUserUseCase viewPublicShopByUserUseCase) {
        this.viewPublicShopByUserUseCase = viewPublicShopByUserUseCase;
    }

    @GetMapping("/{userId}/shop")
    public ResponseEntity<ApiResponse<ViewPublicShopByUserResponse>> viewPublicShopByUser(
            @PathVariable UUID userId
    ) {
        PublicShopByUserSnapshot snapshot = viewPublicShopByUserUseCase.execute(
                new ViewPublicShopByUserCommand(userId)
        );

        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK.value(),
                viewPublicShopByUserUseCase.successMessage(),
                toResponse(snapshot)
        ));
    }

    private ViewPublicShopByUserResponse toResponse(PublicShopByUserSnapshot snapshot) {
        return new ViewPublicShopByUserResponse(
                snapshot.hasShop(),
                snapshot.shopId(),
                snapshot.shopName(),
                snapshot.avatarUrl(),
                snapshot.sellerId()
        );
    }
}
