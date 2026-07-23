package com.twohands.commerce_service.delivery.http.admin;

import com.twohands.commerce_service.application.refund.RefundApprovalStatusParser;
import com.twohands.commerce_service.application.refund.confirmrefundapproval.ConfirmRefundApprovalCommand;
import com.twohands.commerce_service.application.refund.confirmrefundapproval.ConfirmRefundApprovalUseCase;
import com.twohands.commerce_service.application.refund.listadminrefundapprovals.ListAdminRefundApprovalsCommand;
import com.twohands.commerce_service.application.refund.listadminrefundapprovals.ListAdminRefundApprovalsUseCase;
import com.twohands.commerce_service.application.refund.viewadminrefundapproval.ViewAdminRefundApprovalCommand;
import com.twohands.commerce_service.application.refund.viewadminrefundapproval.ViewAdminRefundApprovalUseCase;
import com.twohands.commerce_service.application.refund.rejectrefundapproval.RejectRefundApprovalCommand;
import com.twohands.commerce_service.application.refund.rejectrefundapproval.RejectRefundApprovalUseCase;
import com.twohands.commerce_service.common.dto.ApiResponse;
import com.twohands.commerce_service.domain.order.AdminRefundApprovalItem;
import com.twohands.commerce_service.domain.order.ViewAdminRefundApprovalsResult;
import com.twohands.commerce_service.security.AuthenticatedUser;
import com.twohands.commerce_service.security.CommerceAdminAuthorization;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;
import java.util.Optional;

@RestController
@RequestMapping("/commerce/api/v1/admin/refund-approvals")
public class AdminRefundApprovalController {

    private final ListAdminRefundApprovalsUseCase listAdminRefundApprovalsUseCase;
    private final ViewAdminRefundApprovalUseCase viewAdminRefundApprovalUseCase;
    private final ConfirmRefundApprovalUseCase confirmRefundApprovalUseCase;
    private final RejectRefundApprovalUseCase rejectRefundApprovalUseCase;
    private final CommerceAdminAuthorization commerceAdminAuthorization;

    public AdminRefundApprovalController(
            ListAdminRefundApprovalsUseCase listAdminRefundApprovalsUseCase,
            ViewAdminRefundApprovalUseCase viewAdminRefundApprovalUseCase,
            ConfirmRefundApprovalUseCase confirmRefundApprovalUseCase,
            RejectRefundApprovalUseCase rejectRefundApprovalUseCase,
            CommerceAdminAuthorization commerceAdminAuthorization
    ) {
        this.listAdminRefundApprovalsUseCase = listAdminRefundApprovalsUseCase;
        this.viewAdminRefundApprovalUseCase = viewAdminRefundApprovalUseCase;
        this.confirmRefundApprovalUseCase = confirmRefundApprovalUseCase;
        this.rejectRefundApprovalUseCase = rejectRefundApprovalUseCase;
        this.commerceAdminAuthorization = commerceAdminAuthorization;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<ViewAdminRefundApprovalsResponse>> listRefundApprovals(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String q,
            @RequestParam(name = "requested_by", required = false) String requestedBy,
            @RequestParam(name = "payment_method", required = false) String paymentMethod,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer limit,
            Authentication authentication
    ) {
        AuthenticatedUser admin = resolveAuthenticatedUser(authentication);
        commerceAdminAuthorization.requirePermission(
                admin,
                CommerceAdminAuthorization.PERMISSION_REFUND_SUPPORT_READ
        );

        ViewAdminRefundApprovalsResult result = listAdminRefundApprovalsUseCase.execute(
                new ListAdminRefundApprovalsCommand(
                        RefundApprovalStatusParser.parseOptional(status),
                        Optional.ofNullable(q).filter(value -> !value.isBlank()),
                        Optional.ofNullable(requestedBy).filter(value -> !value.isBlank()),
                        Optional.ofNullable(paymentMethod).filter(value -> !value.isBlank()),
                        Optional.ofNullable(from).filter(value -> !value.isBlank()),
                        Optional.ofNullable(to).filter(value -> !value.isBlank()),
                        page,
                        limit
                )
        );

        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK.value(),
                listAdminRefundApprovalsUseCase.successMessage(),
                ViewAdminRefundApprovalsResponse.from(result)
        ));
    }

    @GetMapping("/{refundRequestId}")
    public ResponseEntity<ApiResponse<AdminRefundApprovalResponse>> getRefundApproval(
            @PathVariable UUID refundRequestId,
            Authentication authentication
    ) {
        AuthenticatedUser admin = resolveAuthenticatedUser(authentication);
        commerceAdminAuthorization.requirePermission(
                admin,
                CommerceAdminAuthorization.PERMISSION_REFUND_SUPPORT_READ
        );

        AdminRefundApprovalItem result = viewAdminRefundApprovalUseCase.execute(
                new ViewAdminRefundApprovalCommand(refundRequestId)
        );

        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK.value(),
                viewAdminRefundApprovalUseCase.successMessage(),
                AdminRefundApprovalResponse.from(result)
        ));
    }

    @PostMapping("/{refundRequestId}/confirm")
    public ResponseEntity<ApiResponse<AdminRefundApprovalResponse>> confirmRefundApproval(
            @PathVariable UUID refundRequestId,
            @RequestBody(required = false) @Valid ConfirmRefundApprovalBody request,
            Authentication authentication
    ) {
        AuthenticatedUser admin = resolveAuthenticatedUser(authentication);
        commerceAdminAuthorization.requirePermission(
                admin,
                CommerceAdminAuthorization.PERMISSION_REFUND_SUPPORT_APPROVE
        );

        String adminNote = request == null || request.adminNote() == null ? "" : request.adminNote();
        AdminRefundApprovalItem result = confirmRefundApprovalUseCase.execute(
                new ConfirmRefundApprovalCommand(refundRequestId, adminNote)
        );

        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK.value(),
                confirmRefundApprovalUseCase.successMessage(),
                AdminRefundApprovalResponse.from(result)
        ));
    }

    @PostMapping("/{refundRequestId}/reject")
    public ResponseEntity<ApiResponse<AdminRefundApprovalResponse>> rejectRefundApproval(
            @PathVariable UUID refundRequestId,
            @RequestBody(required = false) @Valid RejectRefundApprovalBody request,
            Authentication authentication
    ) {
        AuthenticatedUser admin = resolveAuthenticatedUser(authentication);
        commerceAdminAuthorization.requirePermission(
                admin,
                CommerceAdminAuthorization.PERMISSION_REFUND_SUPPORT_APPROVE
        );

        String adminNote = request == null || request.adminNote() == null ? "" : request.adminNote();
        AdminRefundApprovalItem result = rejectRefundApprovalUseCase.execute(
                new RejectRefundApprovalCommand(refundRequestId, adminNote)
        );

        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK.value(),
                rejectRefundApprovalUseCase.successMessage(),
                AdminRefundApprovalResponse.from(result)
        ));
    }

    private AuthenticatedUser resolveAuthenticatedUser(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedUser user)) {
            throw new IllegalStateException("Authenticated admin user is required");
        }
        return user;
    }
}
