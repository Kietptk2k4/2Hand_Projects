package com.twohands.commerce_service.delivery.http.shipping;

import com.twohands.commerce_service.application.shipping.calculateshippingfee.CalculateShippingFeeCommand;
import com.twohands.commerce_service.application.shipping.calculateshippingfee.CalculateShippingFeeResult;
import com.twohands.commerce_service.application.shipping.calculateshippingfee.CalculateShippingFeeUseCase;
import com.twohands.commerce_service.application.shipping.calculateshippingfee.SellerShippingFeeGroupResult;
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
@RequestMapping("/commerce/api/v1/shipping")
public class ShippingController {

    private final CalculateShippingFeeUseCase calculateShippingFeeUseCase;

    public ShippingController(CalculateShippingFeeUseCase calculateShippingFeeUseCase) {
        this.calculateShippingFeeUseCase = calculateShippingFeeUseCase;
    }

    @PostMapping("/fee")
    public ResponseEntity<ApiResponse<CalculateShippingFeeResponse>> calculateShippingFee(
            @RequestBody @Valid CalculateShippingFeeRequest request,
            Authentication authentication
    ) {
        UUID userId = resolveUserId(authentication);
        CalculateShippingFeeResult result = calculateShippingFeeUseCase.execute(
                new CalculateShippingFeeCommand(
                        userId,
                        request.cartItemIds(),
                        request.addressId(),
                        request.shipmentType()
                )
        );

        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK.value(),
                calculateShippingFeeUseCase.successMessage(),
                toResponse(result)
        ));
    }

    private UUID resolveUserId(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedUser principal)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        return principal.userId();
    }

    private CalculateShippingFeeResponse toResponse(CalculateShippingFeeResult result) {
        return new CalculateShippingFeeResponse(
                result.sellerGroups().stream().map(this::toGroupResponse).toList(),
                result.totalShippingFee()
        );
    }

    private SellerShippingFeeGroupResponse toGroupResponse(SellerShippingFeeGroupResult group) {
        return new SellerShippingFeeGroupResponse(
                group.sellerId(),
                group.shopId(),
                group.shippingFee(),
                group.shippingFeeOrigin(),
                group.estimatedDeliveryDate(),
                group.shipmentType()
        );
    }
}
