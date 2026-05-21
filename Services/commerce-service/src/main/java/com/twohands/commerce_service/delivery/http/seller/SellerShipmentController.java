package com.twohands.commerce_service.delivery.http.seller;

import com.twohands.commerce_service.application.shipment.createshipment.CreateShipmentCommand;
import com.twohands.commerce_service.application.shipment.createshipment.CreateShipmentUseCase;
import com.twohands.commerce_service.common.dto.ApiResponse;
import com.twohands.commerce_service.domain.shipment.CreateShipmentResult;
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
@RequestMapping("/commerce/api/v1/seller/shipments")
public class SellerShipmentController {

    private final CreateShipmentUseCase createShipmentUseCase;

    public SellerShipmentController(CreateShipmentUseCase createShipmentUseCase) {
        this.createShipmentUseCase = createShipmentUseCase;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CreateShipmentResponse>> createShipment(
            @RequestBody @Valid CreateShipmentRequest request,
            Authentication authentication
    ) {
        UUID sellerId = resolveUserId(authentication);
        CreateShipmentResult result = createShipmentUseCase.execute(new CreateShipmentCommand(
                sellerId,
                request.orderId(),
                request.orderItemIds(),
                request.carrier(),
                request.shipmentType(),
                request.weightGram(),
                request.trackingNumber()
        ));

        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK.value(),
                createShipmentUseCase.successMessage(),
                toResponse(result)
        ));
    }

    private UUID resolveUserId(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedUser principal)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        return principal.userId();
    }

    private CreateShipmentResponse toResponse(CreateShipmentResult result) {
        return new CreateShipmentResponse(
                result.shipmentId(),
                result.orderId(),
                result.sellerId(),
                result.carrier(),
                result.shipmentType(),
                result.status(),
                result.ghnOrderCode(),
                result.trackingNumber(),
                result.shippingFee(),
                result.codAmount(),
                result.weightGram(),
                result.estimatedDeliveryDate(),
                result.orderItemIds(),
                result.createdAt()
        );
    }
}
