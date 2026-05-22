package com.twohands.commerce_service.delivery.http.cart;

import com.twohands.commerce_service.application.cart.addproducttocart.AddProductToCartCommand;
import com.twohands.commerce_service.application.cart.addproducttocart.AddProductToCartResult;
import com.twohands.commerce_service.application.cart.addproducttocart.AddProductToCartUseCase;
import com.twohands.commerce_service.application.cart.addproducttocart.ProductSummaryResult;
import com.twohands.commerce_service.application.cart.createcart.CreateCartCommand;
import com.twohands.commerce_service.application.cart.createcart.CreateCartItemResult;
import com.twohands.commerce_service.application.cart.createcart.CreateCartResult;
import com.twohands.commerce_service.application.cart.createcart.CreateCartUseCase;
import com.twohands.commerce_service.application.cart.removecartitem.RemoveCartItemCommand;
import com.twohands.commerce_service.application.cart.removecartitem.RemoveCartItemResult;
import com.twohands.commerce_service.application.cart.removecartitem.RemoveCartItemUseCase;
import com.twohands.commerce_service.application.cart.updatecartitemquantity.UpdateCartItemQuantityCommand;
import com.twohands.commerce_service.application.cart.updatecartitemquantity.UpdateCartItemQuantityResult;
import com.twohands.commerce_service.application.cart.updatecartitemquantity.UpdateCartItemQuantityUseCase;
import com.twohands.commerce_service.application.cart.validatecartitems.ValidateCartItemsCommand;
import com.twohands.commerce_service.application.cart.validatecartitems.ValidateCartItemsUseCase;
import com.twohands.commerce_service.domain.cart.InvalidCartItem;
import com.twohands.commerce_service.domain.cart.ValidateCartItemsResult;
import com.twohands.commerce_service.domain.cart.ValidCartItem;
import com.twohands.commerce_service.common.dto.ApiResponse;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import com.twohands.commerce_service.security.AuthenticatedUser;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/commerce/api/v1/cart")
public class CartController {

    private final CreateCartUseCase createCartUseCase;
    private final AddProductToCartUseCase addProductToCartUseCase;
    private final RemoveCartItemUseCase removeCartItemUseCase;
    private final UpdateCartItemQuantityUseCase updateCartItemQuantityUseCase;
    private final ValidateCartItemsUseCase validateCartItemsUseCase;

    public CartController(
            CreateCartUseCase createCartUseCase,
            AddProductToCartUseCase addProductToCartUseCase,
            RemoveCartItemUseCase removeCartItemUseCase,
            UpdateCartItemQuantityUseCase updateCartItemQuantityUseCase,
            ValidateCartItemsUseCase validateCartItemsUseCase
    ) {
        this.createCartUseCase = createCartUseCase;
        this.addProductToCartUseCase = addProductToCartUseCase;
        this.removeCartItemUseCase = removeCartItemUseCase;
        this.updateCartItemQuantityUseCase = updateCartItemQuantityUseCase;
        this.validateCartItemsUseCase = validateCartItemsUseCase;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CreateCartResponse>> createCart(Authentication authentication) {
        UUID userId = resolveUserId(authentication);
        CreateCartResult result = createCartUseCase.execute(new CreateCartCommand(userId));
        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK.value(),
                createCartUseCase.successMessage(result.newlyCreated()),
                toCreateCartResponse(result)
        ));
    }

    @PatchMapping("/items/{cartItemId}")
    public ResponseEntity<ApiResponse<UpdateCartItemQuantityResponse>> updateCartItemQuantity(
            @PathVariable UUID cartItemId,
            @RequestBody @Valid UpdateCartItemQuantityRequest request,
            Authentication authentication
    ) {
        UUID userId = resolveUserId(authentication);
        UpdateCartItemQuantityResult result = updateCartItemQuantityUseCase.execute(
                new UpdateCartItemQuantityCommand(userId, cartItemId, request.quantity())
        );

        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK.value(),
                updateCartItemQuantityUseCase.successMessage(),
                toUpdateQuantityResponse(result)
        ));
    }

    @DeleteMapping("/items/{cartItemId}")
    public ResponseEntity<ApiResponse<RemoveCartItemResponse>> removeCartItem(
            @PathVariable UUID cartItemId,
            Authentication authentication
    ) {
        UUID userId = resolveUserId(authentication);
        RemoveCartItemResult result = removeCartItemUseCase.execute(new RemoveCartItemCommand(userId, cartItemId));

        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK.value(),
                removeCartItemUseCase.successMessage(result.alreadyRemoved()),
                toRemoveResponse(result)
        ));
    }

    @PostMapping("/validate")
    public ResponseEntity<ApiResponse<ValidateCartItemsResponse>> validateCartItems(
            @RequestBody(required = false) ValidateCartItemsRequest request,
            Authentication authentication
    ) {
        UUID userId = resolveUserId(authentication);
        ValidateCartItemsResult result = validateCartItemsUseCase.execute(
                new ValidateCartItemsCommand(
                        userId,
                        request == null ? null : request.cartItemIds()
                )
        );

        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK.value(),
                validateCartItemsUseCase.successMessage(),
                toValidateResponse(result)
        ));
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

    private CreateCartResponse toCreateCartResponse(CreateCartResult result) {
        return new CreateCartResponse(
                result.cartId(),
                result.userId(),
                result.items().stream().map(this::toCreateCartItemResponse).toList(),
                result.createdAt(),
                result.updatedAt()
        );
    }

    private CreateCartItemResponse toCreateCartItemResponse(CreateCartItemResult item) {
        return new CreateCartItemResponse(
                item.cartItemId(),
                item.productId(),
                item.sellerId(),
                item.quantity(),
                item.status()
        );
    }

    private RemoveCartItemResponse toRemoveResponse(RemoveCartItemResult result) {
        return new RemoveCartItemResponse(
                result.cartId(),
                result.cartItemId(),
                result.productId(),
                result.status(),
                result.removedAt(),
                new CartSummaryResponse(result.activeItemCount()),
                result.alreadyRemoved()
        );
    }

    private UpdateCartItemQuantityResponse toUpdateQuantityResponse(UpdateCartItemQuantityResult result) {
        ProductSummaryResult product = result.product();
        return new UpdateCartItemQuantityResponse(
                result.cartId(),
                result.cartItemId(),
                result.productId(),
                result.quantity(),
                result.status(),
                new ProductSummaryResponse(
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
                ),
                new CartSummaryResponse(result.activeItemCount())
        );
    }

    private ValidateCartItemsResponse toValidateResponse(ValidateCartItemsResult result) {
        return new ValidateCartItemsResponse(
                result.validItems().stream().map(this::toValidItemResponse).toList(),
                result.invalidItems().stream().map(this::toInvalidItemResponse).toList(),
                result.canCheckout()
        );
    }

    private ValidCartItemResponse toValidItemResponse(ValidCartItem item) {
        return new ValidCartItemResponse(item.cartItemId(), item.currentStatus().name());
    }

    private InvalidCartItemResponse toInvalidItemResponse(InvalidCartItem item) {
        return new InvalidCartItemResponse(item.cartItemId(), item.reason(), item.currentStatus().name());
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
