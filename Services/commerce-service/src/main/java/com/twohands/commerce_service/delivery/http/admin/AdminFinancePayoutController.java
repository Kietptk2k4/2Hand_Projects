package com.twohands.commerce_service.delivery.http.admin;

import com.twohands.commerce_service.application.finance.payout.PayoutRequestStatusParser;
import com.twohands.commerce_service.application.finance.payout.approvepayoutrequest.ApprovePayoutRequestCommand;
import com.twohands.commerce_service.application.finance.payout.approvepayoutrequest.ApprovePayoutRequestUseCase;
import com.twohands.commerce_service.application.finance.payout.listadminpayoutrequests.ListAdminPayoutRequestsCommand;
import com.twohands.commerce_service.application.finance.payout.listadminpayoutrequests.ListAdminPayoutRequestsUseCase;
import com.twohands.commerce_service.application.finance.payout.markpayoutrequestpaid.MarkPayoutRequestPaidCommand;
import com.twohands.commerce_service.application.finance.payout.markpayoutrequestpaid.MarkPayoutRequestPaidUseCase;
import com.twohands.commerce_service.application.finance.payout.rejectpayoutrequest.RejectPayoutRequestCommand;
import com.twohands.commerce_service.application.finance.payout.rejectpayoutrequest.RejectPayoutRequestUseCase;
import com.twohands.commerce_service.common.dto.ApiResponse;
import com.twohands.commerce_service.delivery.http.seller.SellerPayoutRequestResponse;
import com.twohands.commerce_service.delivery.http.seller.ViewSellerPayoutRequestsResponse;
import com.twohands.commerce_service.domain.finance.SellerPayoutRequest;
import com.twohands.commerce_service.domain.finance.ViewSellerPayoutRequestsResult;
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
@RequestMapping("/commerce/api/v1/admin/finance/payout-requests")
public class AdminFinancePayoutController {

    private final ListAdminPayoutRequestsUseCase listAdminPayoutRequestsUseCase;
    private final ApprovePayoutRequestUseCase approvePayoutRequestUseCase;
    private final RejectPayoutRequestUseCase rejectPayoutRequestUseCase;
    private final MarkPayoutRequestPaidUseCase markPayoutRequestPaidUseCase;
    private final CommerceAdminAuthorization commerceAdminAuthorization;

    public AdminFinancePayoutController(
            ListAdminPayoutRequestsUseCase listAdminPayoutRequestsUseCase,
            ApprovePayoutRequestUseCase approvePayoutRequestUseCase,
            RejectPayoutRequestUseCase rejectPayoutRequestUseCase,
            MarkPayoutRequestPaidUseCase markPayoutRequestPaidUseCase,
            CommerceAdminAuthorization commerceAdminAuthorization
    ) {
        this.listAdminPayoutRequestsUseCase = listAdminPayoutRequestsUseCase;
        this.approvePayoutRequestUseCase = approvePayoutRequestUseCase;
        this.rejectPayoutRequestUseCase = rejectPayoutRequestUseCase;
        this.markPayoutRequestPaidUseCase = markPayoutRequestPaidUseCase;
        this.commerceAdminAuthorization = commerceAdminAuthorization;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<ViewSellerPayoutRequestsResponse>> listPayoutRequests(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer limit,
            Authentication authentication
    ) {
        AuthenticatedUser admin = resolveAuthenticatedUser(authentication);
        commerceAdminAuthorization.requirePermission(
                admin,
                CommerceAdminAuthorization.PERMISSION_PAYOUT_SUPPORT_READ
        );

        ViewSellerPayoutRequestsResult result = listAdminPayoutRequestsUseCase.execute(
                new ListAdminPayoutRequestsCommand(
                        PayoutRequestStatusParser.parseOptional(status),
                        page,
                        limit
                )
        );

        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK.value(),
                listAdminPayoutRequestsUseCase.successMessage(),
                ViewSellerPayoutRequestsResponse.from(result)
        ));
    }

    @PostMapping("/{payoutRequestId}/approve")
    public ResponseEntity<ApiResponse<SellerPayoutRequestResponse>> approvePayoutRequest(
            @PathVariable UUID payoutRequestId,
            Authentication authentication
    ) {
        AuthenticatedUser admin = resolveAuthenticatedUser(authentication);
        commerceAdminAuthorization.requirePermission(
                admin,
                CommerceAdminAuthorization.PERMISSION_PAYOUT_SUPPORT_APPROVE
        );

        SellerPayoutRequest result = approvePayoutRequestUseCase.execute(
                new ApprovePayoutRequestCommand(payoutRequestId)
        );

        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK.value(),
                approvePayoutRequestUseCase.successMessage(),
                SellerPayoutRequestResponse.from(result)
        ));
    }

    @PostMapping("/{payoutRequestId}/reject")
    public ResponseEntity<ApiResponse<SellerPayoutRequestResponse>> rejectPayoutRequest(
            @PathVariable UUID payoutRequestId,
            @RequestBody(required = false) @Valid RejectPayoutRequestBody request,
            Authentication authentication
    ) {
        AuthenticatedUser admin = resolveAuthenticatedUser(authentication);
        commerceAdminAuthorization.requirePermission(
                admin,
                CommerceAdminAuthorization.PERMISSION_PAYOUT_SUPPORT_APPROVE
        );

        String adminNote = request == null || request.adminNote() == null ? "" : request.adminNote();
        SellerPayoutRequest result = rejectPayoutRequestUseCase.execute(
                new RejectPayoutRequestCommand(payoutRequestId, adminNote)
        );

        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK.value(),
                rejectPayoutRequestUseCase.successMessage(),
                SellerPayoutRequestResponse.from(result)
        ));
    }

    @PostMapping("/{payoutRequestId}/mark-paid")
    public ResponseEntity<ApiResponse<SellerPayoutRequestResponse>> markPayoutRequestPaid(
            @PathVariable UUID payoutRequestId,
            @RequestBody @Valid MarkPayoutRequestPaidRequest request,
            Authentication authentication
    ) {
        AuthenticatedUser admin = resolveAuthenticatedUser(authentication);
        commerceAdminAuthorization.requirePermission(
                admin,
                CommerceAdminAuthorization.PERMISSION_PAYOUT_SUPPORT_APPROVE
        );

        SellerPayoutRequest result = markPayoutRequestPaidUseCase.execute(
                new MarkPayoutRequestPaidCommand(payoutRequestId, request.bankTransferRef())
        );

        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK.value(),
                markPayoutRequestPaidUseCase.successMessage(),
                SellerPayoutRequestResponse.from(result)
        ));
    }

    private AuthenticatedUser resolveAuthenticatedUser(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedUser user)) {
            throw new IllegalStateException("Authenticated admin user is required");
        }
        return user;
    }
}
