package com.twohands.commerce_service.delivery.http.order;

import com.twohands.commerce_service.application.order.cancelorder.CancelOrderCommand;
import com.twohands.commerce_service.application.order.cancelorder.CancelOrderResult;
import com.twohands.commerce_service.application.order.cancelorder.CancelOrderUseCase;
import com.twohands.commerce_service.common.dto.ApiResponse;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import com.twohands.commerce_service.security.AuthenticatedUser;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/commerce/api/v1/orders")
public class OrderController {

    private final CancelOrderUseCase cancelOrderUseCase;

    public OrderController(CancelOrderUseCase cancelOrderUseCase) {
        this.cancelOrderUseCase = cancelOrderUseCase;
    }

    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<ApiResponse<CancelOrderResponse>> cancelOrder(
            @PathVariable UUID orderId,
            @RequestBody(required = false) CancelOrderRequest request,
            Authentication authentication
    ) {
        UUID buyerId = resolveUserId(authentication);
        String reason = request == null ? null : request.reason();

        CancelOrderResult result = cancelOrderUseCase.execute(
                new CancelOrderCommand(buyerId, orderId, reason)
        );

        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK.value(),
                cancelOrderUseCase.successMessage(result.alreadyCancelled()),
                new CancelOrderResponse(result.orderId(), result.status(), result.cancelledAt())
        ));
    }

    private UUID resolveUserId(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedUser principal)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        return principal.userId();
    }
}
