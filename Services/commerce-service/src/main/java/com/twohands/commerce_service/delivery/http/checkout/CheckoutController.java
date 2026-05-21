package com.twohands.commerce_service.delivery.http.checkout;

import com.twohands.commerce_service.application.checkout.calculateordertotal.CalculateOrderTotalCommand;
import com.twohands.commerce_service.application.checkout.calculateordertotal.CalculateOrderTotalResult;
import com.twohands.commerce_service.application.checkout.calculateordertotal.CalculateOrderTotalUseCase;
import com.twohands.commerce_service.application.checkout.calculateordertotal.QuoteItemResult;
import com.twohands.commerce_service.application.checkout.calculateordertotal.SellerShippingGroupResult;
import com.twohands.commerce_service.application.checkout.checkoutfromcart.CheckoutFromCartCommand;
import com.twohands.commerce_service.application.checkout.checkoutfromcart.CheckoutFromCartUseCase;
import com.twohands.commerce_service.domain.checkout.CheckoutFromCartResult;
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
@RequestMapping("/commerce/api/v1/checkout")
public class CheckoutController {

    private final CalculateOrderTotalUseCase calculateOrderTotalUseCase;
    private final CheckoutFromCartUseCase checkoutFromCartUseCase;

    public CheckoutController(
            CalculateOrderTotalUseCase calculateOrderTotalUseCase,
            CheckoutFromCartUseCase checkoutFromCartUseCase
    ) {
        this.calculateOrderTotalUseCase = calculateOrderTotalUseCase;
        this.checkoutFromCartUseCase = checkoutFromCartUseCase;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CheckoutFromCartResponse>> checkoutFromCart(
            @RequestBody @Valid CheckoutFromCartRequest request,
            Authentication authentication
    ) {
        UUID userId = resolveUserId(authentication);
        CheckoutFromCartResult result = checkoutFromCartUseCase.execute(
                new CheckoutFromCartCommand(
                        userId,
                        request.cartItemIds(),
                        request.addressId(),
                        request.paymentMethod(),
                        request.shipmentType(),
                        request.idempotencyKey()
                )
        );

        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK.value(),
                checkoutFromCartUseCase.successMessage(result.idempotentReplay()),
                toCheckoutResponse(result)
        ));
    }

    @PostMapping("/quote")
    public ResponseEntity<ApiResponse<CalculateOrderTotalResponse>> calculateOrderTotal(
            @RequestBody @Valid CalculateOrderTotalRequest request,
            Authentication authentication
    ) {
        UUID userId = resolveUserId(authentication);
        CalculateOrderTotalResult result = calculateOrderTotalUseCase.execute(
                new CalculateOrderTotalCommand(
                        userId,
                        request.cartItemIds(),
                        request.addressId(),
                        request.shipmentType()
                )
        );

        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK.value(),
                calculateOrderTotalUseCase.successMessage(),
                toResponse(result)
        ));
    }

    private UUID resolveUserId(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedUser principal)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        return principal.userId();
    }

    private CalculateOrderTotalResponse toResponse(CalculateOrderTotalResult result) {
        return new CalculateOrderTotalResponse(
                result.items().stream().map(this::toItemResponse).toList(),
                result.totalAmount(),
                result.shippingFee(),
                result.finalAmount(),
                result.sellerShippingGroups().stream().map(this::toGroupResponse).toList()
        );
    }

    private QuoteItemResponse toItemResponse(QuoteItemResult item) {
        return new QuoteItemResponse(
                item.cartItemId(),
                item.unitPrice(),
                item.quantity(),
                item.itemTotal(),
                item.shippingFeeAllocated()
        );
    }

    private SellerShippingGroupResponse toGroupResponse(SellerShippingGroupResult group) {
        return new SellerShippingGroupResponse(
                group.sellerId(),
                group.shopId(),
                group.shippingFee(),
                group.shipmentType()
        );
    }

    private CheckoutFromCartResponse toCheckoutResponse(CheckoutFromCartResult result) {
        return new CheckoutFromCartResponse(
                result.orderId(),
                result.paymentId(),
                result.paymentMethod(),
                result.paymentStatus(),
                result.orderStatus(),
                result.finalAmount(),
                result.payosCheckoutUrl()
        );
    }
}
