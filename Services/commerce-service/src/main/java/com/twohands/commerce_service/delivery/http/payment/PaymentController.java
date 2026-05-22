package com.twohands.commerce_service.delivery.http.payment;

import com.fasterxml.jackson.databind.JsonNode;
import com.twohands.commerce_service.application.payment.createpayoscheckouturl.CreatePayosCheckoutUrlCommand;
import com.twohands.commerce_service.application.payment.createpayoscheckouturl.CreatePayosCheckoutUrlUseCase;
import com.twohands.commerce_service.application.payment.viewpaymentstatus.ViewPaymentStatusCommand;
import com.twohands.commerce_service.application.payment.viewpaymentstatus.ViewPaymentStatusUseCase;
import com.twohands.commerce_service.application.payment.processpayoswebhook.ProcessPayosWebhookResult;
import com.twohands.commerce_service.application.payment.processpayoswebhook.ProcessPayosWebhookUseCase;
import com.twohands.commerce_service.common.dto.ApiResponse;
import com.twohands.commerce_service.domain.payment.CreatePayosCheckoutUrlResult;
import com.twohands.commerce_service.domain.payment.ViewPaymentStatusResult;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import com.twohands.commerce_service.security.AuthenticatedUser;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/commerce/api/v1/payments")
public class PaymentController {

    private final CreatePayosCheckoutUrlUseCase createPayosCheckoutUrlUseCase;
    private final ProcessPayosWebhookUseCase processPayosWebhookUseCase;
    private final ViewPaymentStatusUseCase viewPaymentStatusUseCase;

    public PaymentController(
            CreatePayosCheckoutUrlUseCase createPayosCheckoutUrlUseCase,
            ProcessPayosWebhookUseCase processPayosWebhookUseCase,
            ViewPaymentStatusUseCase viewPaymentStatusUseCase
    ) {
        this.createPayosCheckoutUrlUseCase = createPayosCheckoutUrlUseCase;
        this.processPayosWebhookUseCase = processPayosWebhookUseCase;
        this.viewPaymentStatusUseCase = viewPaymentStatusUseCase;
    }

    @GetMapping("/{paymentId}/status")
    public ResponseEntity<ApiResponse<ViewPaymentStatusResponse>> viewPaymentStatus(
            @PathVariable UUID paymentId,
            Authentication authentication
    ) {
        UUID buyerId = resolveUserId(authentication);
        ViewPaymentStatusResult result = viewPaymentStatusUseCase.execute(
                new ViewPaymentStatusCommand(buyerId, paymentId)
        );

        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK.value(),
                viewPaymentStatusUseCase.successMessage(),
                toViewPaymentStatusResponse(result)
        ));
    }

    @PostMapping("/webhooks/payos")
    public ResponseEntity<ApiResponse<PayosWebhookResponse>> processPayosWebhook(
            @RequestBody JsonNode webhookBody
    ) {
        ProcessPayosWebhookResult result = processPayosWebhookUseCase.execute(webhookBody);
        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK.value(),
                "Da nhan PayOS webhook.",
                new PayosWebhookResponse(
                        result.eventType(),
                        result.payosOrderCode(),
                        result.signatureValid(),
                        result.processed(),
                        result.terminalStatus() != null ? result.terminalStatus().name() : null,
                        result.failureOutcome() != null ? result.failureOutcome().name() : null,
                        result.successWebhook()
                )
        ));
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

    private ViewPaymentStatusResponse toViewPaymentStatusResponse(ViewPaymentStatusResult result) {
        return new ViewPaymentStatusResponse(
                result.paymentId(),
                result.orderId(),
                result.paymentMethod(),
                result.amount(),
                result.currency(),
                result.status(),
                result.paidAt(),
                result.expiredAt(),
                result.payosCheckoutUrl(),
                result.orderStatus(),
                result.orderPaymentStatus()
        );
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
