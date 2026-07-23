package com.twohands.commerce_service.delivery.http.admin;

import com.twohands.commerce_service.application.payment.viewpaymentsupport.ViewPaymentSupportDetailCommand;
import com.twohands.commerce_service.application.payment.viewpaymentsupport.ViewPaymentSupportDetailUseCase;
import com.twohands.commerce_service.application.payment.viewpaymentsupport.ViewPaymentsForSupportQuery;
import com.twohands.commerce_service.application.payment.viewpaymentsupport.ViewPaymentsForSupportResult;
import com.twohands.commerce_service.application.payment.viewpaymentsupport.ViewPaymentsForSupportUseCase;
import com.twohands.commerce_service.common.dto.ApiResponse;
import com.twohands.commerce_service.delivery.http.payment.ViewPaymentSupportDetailResponse;
import com.twohands.commerce_service.delivery.http.payment.ViewPaymentsForSupportResponse;
import com.twohands.commerce_service.domain.payment.ViewPaymentSupportDetailResult;
import com.twohands.commerce_service.security.AuthenticatedUser;
import com.twohands.commerce_service.security.CommerceAdminAuthorization;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/commerce/api/v1/admin/support/payments")
public class AdminPaymentSupportController {

    private final ViewPaymentSupportDetailUseCase viewPaymentSupportDetailUseCase;
    private final ViewPaymentsForSupportUseCase viewPaymentsForSupportUseCase;
    private final CommerceAdminAuthorization commerceAdminAuthorization;

    public AdminPaymentSupportController(
            ViewPaymentSupportDetailUseCase viewPaymentSupportDetailUseCase,
            ViewPaymentsForSupportUseCase viewPaymentsForSupportUseCase,
            CommerceAdminAuthorization commerceAdminAuthorization
    ) {
        this.viewPaymentSupportDetailUseCase = viewPaymentSupportDetailUseCase;
        this.viewPaymentsForSupportUseCase = viewPaymentsForSupportUseCase;
        this.commerceAdminAuthorization = commerceAdminAuthorization;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<ViewPaymentsForSupportResponse>> listPaymentsForSupport(
            @RequestParam(required = false) String status,
            @RequestParam(name = "payment_method", required = false) String paymentMethod,
            @RequestParam(name = "order_id", required = false) String orderId,
            @RequestParam(required = false) String q,
            @RequestParam(name = "reconciliation_status", required = false) String reconciliationStatus,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            Authentication authentication
    ) {
        AuthenticatedUser admin = resolveAuthenticatedUser(authentication);
        commerceAdminAuthorization.requirePermission(
                admin,
                CommerceAdminAuthorization.PERMISSION_PAYMENT_SUPPORT_READ
        );

        ViewPaymentsForSupportResult result = viewPaymentsForSupportUseCase.execute(
                new ViewPaymentsForSupportQuery(
                        status,
                        paymentMethod,
                        orderId,
                        q,
                        reconciliationStatus,
                        from,
                        to,
                        page,
                        size
                )
        );

        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK.value(),
                viewPaymentsForSupportUseCase.successMessage(),
                ViewPaymentsForSupportResponse.from(result)
        ));
    }

    @GetMapping("/{paymentId}")
    public ResponseEntity<ApiResponse<ViewPaymentSupportDetailResponse>> viewPaymentSupportDetail(
            @PathVariable UUID paymentId,
            Authentication authentication
    ) {
        AuthenticatedUser admin = resolveAuthenticatedUser(authentication);
        commerceAdminAuthorization.requirePermission(
                admin,
                CommerceAdminAuthorization.PERMISSION_PAYMENT_SUPPORT_READ
        );

        ViewPaymentSupportDetailResult result = viewPaymentSupportDetailUseCase.execute(
                new ViewPaymentSupportDetailCommand(paymentId)
        );

        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK.value(),
                viewPaymentSupportDetailUseCase.successMessage(),
                ViewPaymentSupportDetailResponse.from(result)
        ));
    }

    private AuthenticatedUser resolveAuthenticatedUser(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedUser user)) {
            throw new IllegalStateException("Authenticated admin user is required");
        }
        return user;
    }
}
