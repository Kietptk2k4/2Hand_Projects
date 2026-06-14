package com.twohands.commerce_service.delivery.http.admin;

import com.twohands.commerce_service.application.refund.RefundApprovalStatusParser;
import com.twohands.commerce_service.application.refund.confirmrefundapproval.ConfirmRefundApprovalCommand;
import com.twohands.commerce_service.application.refund.confirmrefundapproval.ConfirmRefundApprovalUseCase;
import com.twohands.commerce_service.application.refund.listadminrefundapprovals.ListAdminRefundApprovalsCommand;
import com.twohands.commerce_service.application.refund.listadminrefundapprovals.ListAdminRefundApprovalsUseCase;
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

@RestController
@RequestMapping("/commerce/api/v1/admin/refund-approvals")
public class AdminRefundApprovalController {

    private final ListAdminRefundApprovalsUseCase listAdminRefundApprovalsUseCase;
    private final ConfirmRefundApprovalUseCase confirmRefundApprovalUseCase;
    private final RejectRefundApprovalUseCase rejectRefundApprovalUseCase;
    private final CommerceAdminAuthorization commerceAdminAuthorization;

    public AdminRefundApprovalController(
            ListAdminRefundApprovalsUseCase listAdminRefundApprovalsUseCase,
            ConfirmRefundApprovalUseCase confirmRefundApprovalUseCase,
            RejectRefundApprovalUseCase rejectRefundApprovalUseCase,
            CommerceAdminAuthorization commerceAdminAuthorization
    ) {
        this.listAdminRefundApprovalsUseCase = listAdminRefundApprovalsUseCase;
        this.confirmRefundApprovalUseCase = confirmRefundApprovalUseCase;
        this.rejectRefundApprovalUseCase = rejectRefundApprovalUseCase;
        this.commerceAdminAuthorization = commerceAdminAuthorization;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<ViewAdminRefundApprovalsResponse>> listRefundApprovals(
            @RequestParam(required = false) String status,
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
