package com.twohands.commerce_service.delivery.http.cart;

import com.twohands.commerce_service.application.cart.addproducttocart.AddProductToCartCommand;
import com.twohands.commerce_service.application.cart.addproducttocart.AddProductToCartResult;
import com.twohands.commerce_service.application.cart.addproducttocart.AddProductToCartUseCase;
import com.twohands.commerce_service.application.cart.addproducttocart.ProductSummaryResult;
import com.twohands.commerce_service.common.dto.ApiResponse;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import com.twohands.commerce_service.security.AuthenticatedUser;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/commerce/api/v1/cart")
public class CartController {

    private final AddProductToCartUseCase addProductToCartUseCase;

    public CartController(AddProductToCartUseCase addProductToCartUseCase) {
        this.addProductToCartUseCase = addProductToCartUseCase;
    }

    @PostMapping("/items")
    public ResponseEntity<ApiResponse<AddProductToCartResponse>> addProductToCart(
            @RequestBody @Valid AddProductToCartRequest request,
            Authentication authentication
    ) {
        UUID userId = resolveUserId(authentication);
        AddProductToCartResult result = addProductToCartUseCase.execute(
                new AddProductToCartCommand(userId, request.productId(), request.quantity())
        );

        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK.value(),
                addProductToCartUseCase.successMessage(),
                toResponse(result)
        ));
    }

    private UUID resolveUserId(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedUser principal)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        return principal.userId();
    }

    private AddProductToCartResponse toResponse(AddProductToCartResult result) {
        ProductSummaryResult product = result.product();
        ProductSummaryResponse productResponse = new ProductSummaryResponse(
                product.productId(),
                product.sellerId(),
                product.shopId(),
                product.productName(),
                product.imageUrl(),
                product.price(),
                product.salePrice(),
                product.effectivePrice(),
                product.inStock(),
                product.availableQuantity()
        );

        return new AddProductToCartResponse(
                result.cartId(),
                result.cartItemId(),
                result.productId(),
                result.quantity(),
                result.status(),
                productResponse
        );
    }
}
