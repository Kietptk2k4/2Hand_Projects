package com.twohands.commerce_service.delivery.http.payment;

import com.twohands.commerce_service.application.payment.createpayoscheckouturl.CreatePayosCheckoutUrlCommand;
import com.twohands.commerce_service.application.payment.createpayoscheckouturl.CreatePayosCheckoutUrlUseCase;
import com.twohands.commerce_service.common.dto.ApiResponse;
import com.twohands.commerce_service.domain.payment.CreatePayosCheckoutUrlResult;
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
@RequestMapping("/commerce/api/v1/payments")
public class PaymentController {

    private final CreatePayosCheckoutUrlUseCase createPayosCheckoutUrlUseCase;

    public PaymentController(CreatePayosCheckoutUrlUseCase createPayosCheckoutUrlUseCase) {
        this.createPayosCheckoutUrlUseCase = createPayosCheckoutUrlUseCase;
    }

    @PostMapping("/{paymentId}/payos-checkout-url")
    public ResponseEntity<ApiResponse<CreatePayosCheckoutUrlResponse>> createPayosCheckoutUrl(
            @PathVariable UUID paymentId,
            Authentication authentication
    ) {
        UUID buyerId = resolveUserId(authentication);
        CreatePayosCheckoutUrlResult result = createPayosCheckoutUrlUseCase.execute(
                new CreatePayosCheckoutUrlCommand(paymentId, buyerId)
        );

        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK.value(),
                createPayosCheckoutUrlUseCase.successMessage(result.reusedExistingUrl()),
                toResponse(result)
        ));
    }

    private UUID resolveUserId(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedUser principal)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        return principal.userId();
    }

    private CreatePayosCheckoutUrlResponse toResponse(CreatePayosCheckoutUrlResult result) {
        return new CreatePayosCheckoutUrlResponse(
                result.paymentId(),
                result.orderId(),
                result.payosOrderCode(),
                result.payosCheckoutUrl(),
                result.checkoutUrlExpiredAt()
        );
    }
}
