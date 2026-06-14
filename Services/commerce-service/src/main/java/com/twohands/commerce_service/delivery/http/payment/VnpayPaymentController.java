package com.twohands.commerce_service.delivery.http.payment;

import com.twohands.commerce_service.application.payment.createvnpaycheckouturl.CreateVnpayCheckoutUrlCommand;
import com.twohands.commerce_service.application.payment.createvnpaycheckouturl.CreateVnpayCheckoutUrlUseCase;
import com.twohands.commerce_service.application.payment.processvnpayreturn.ProcessVnpayReturnResult;
import com.twohands.commerce_service.application.payment.processvnpayreturn.ProcessVnpayReturnUseCase;
import com.twohands.commerce_service.domain.payment.CreateVnpayCheckoutUrlResult;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import com.twohands.commerce_service.security.AuthenticatedUser;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/commerce/api/v1/payments")
public class VnpayPaymentController {

    private final ProcessVnpayReturnUseCase processVnpayReturnUseCase;
    private final CreateVnpayCheckoutUrlUseCase createVnpayCheckoutUrlUseCase;

    public VnpayPaymentController(
            ProcessVnpayReturnUseCase processVnpayReturnUseCase,
            CreateVnpayCheckoutUrlUseCase createVnpayCheckoutUrlUseCase
    ) {
        this.processVnpayReturnUseCase = processVnpayReturnUseCase;
        this.createVnpayCheckoutUrlUseCase = createVnpayCheckoutUrlUseCase;
    }

    @GetMapping("/vnpay/return")
    public ResponseEntity<Void> processVnpayReturn(HttpServletRequest request) {
        ProcessVnpayReturnResult result = processVnpayReturnUseCase.execute(extractQueryParams(request));
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(result.redirectUri())
                .build();
    }

    @PostMapping("/{paymentId}/vnpay-checkout-url")
    public ResponseEntity<com.twohands.commerce_service.common.dto.ApiResponse<CreateVnpayCheckoutUrlResponse>> createVnpayCheckoutUrl(
            @PathVariable UUID paymentId,
            Authentication authentication,
            HttpServletRequest request
    ) {
        UUID buyerId = resolveUserId(authentication);
        CreateVnpayCheckoutUrlResult result = createVnpayCheckoutUrlUseCase.execute(
                new CreateVnpayCheckoutUrlCommand(paymentId, buyerId, resolveClientIp(request))
        );

        return ResponseEntity.ok(com.twohands.commerce_service.common.dto.ApiResponse.success(
                HttpStatus.OK.value(),
                "Tao link thanh toan VNPay thanh cong.",
                toResponse(result)
        ));
    }

    private UUID resolveUserId(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedUser principal)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        return principal.userId();
    }

    private CreateVnpayCheckoutUrlResponse toResponse(CreateVnpayCheckoutUrlResult result) {
        return new CreateVnpayCheckoutUrlResponse(
                result.paymentId(),
                result.orderId(),
                result.txnRef(),
                result.checkoutUrl()
        );
    }

    private Map<String, String> extractQueryParams(HttpServletRequest request) {
        Map<String, String> params = new LinkedHashMap<>();
        request.getParameterMap().forEach((key, values) -> {
            if (values != null && values.length > 0) {
                params.put(key, values[0]);
            }
        });
        return params;
    }

    private String resolveClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded;
        }
        return request.getRemoteAddr();
    }
}
