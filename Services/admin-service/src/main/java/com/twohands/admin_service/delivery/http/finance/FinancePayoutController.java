package com.twohands.admin_service.delivery.http.finance;

import com.twohands.admin_service.application.finance.approvepayoutrequest.ApproveAdminFinancePayoutCommand;
import com.twohands.admin_service.application.finance.approvepayoutrequest.ApproveAdminFinancePayoutUseCase;
import com.twohands.admin_service.application.finance.listpayoutrequests.ListAdminFinancePayoutRequestsQuery;
import com.twohands.admin_service.application.finance.listpayoutrequests.ListAdminFinancePayoutRequestsUseCase;
import com.twohands.admin_service.application.finance.markpayoutpaid.MarkAdminFinancePayoutPaidCommand;
import com.twohands.admin_service.application.finance.markpayoutpaid.MarkAdminFinancePayoutPaidUseCase;
import com.twohands.admin_service.application.finance.rejectpayoutrequest.RejectAdminFinancePayoutCommand;
import com.twohands.admin_service.application.finance.rejectpayoutrequest.RejectAdminFinancePayoutUseCase;
import com.twohands.admin_service.common.dto.ApiResponse;
import com.twohands.admin_service.constant.AdminPermission;
import com.twohands.admin_service.domain.payout.AdminPayoutRequestItem;
import com.twohands.admin_service.domain.payout.AdminPayoutRequestListResult;
import com.twohands.admin_service.security.annotation.RequireAdminPermission;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/admin/api/v1/finance/payout-requests")
public class FinancePayoutController {

	private final ListAdminFinancePayoutRequestsUseCase listAdminFinancePayoutRequestsUseCase;
	private final ApproveAdminFinancePayoutUseCase approveAdminFinancePayoutUseCase;
	private final RejectAdminFinancePayoutUseCase rejectAdminFinancePayoutUseCase;
	private final MarkAdminFinancePayoutPaidUseCase markAdminFinancePayoutPaidUseCase;

	public FinancePayoutController(
			ListAdminFinancePayoutRequestsUseCase listAdminFinancePayoutRequestsUseCase,
			ApproveAdminFinancePayoutUseCase approveAdminFinancePayoutUseCase,
			RejectAdminFinancePayoutUseCase rejectAdminFinancePayoutUseCase,
			MarkAdminFinancePayoutPaidUseCase markAdminFinancePayoutPaidUseCase
	) {
		this.listAdminFinancePayoutRequestsUseCase = listAdminFinancePayoutRequestsUseCase;
		this.approveAdminFinancePayoutUseCase = approveAdminFinancePayoutUseCase;
		this.rejectAdminFinancePayoutUseCase = rejectAdminFinancePayoutUseCase;
		this.markAdminFinancePayoutPaidUseCase = markAdminFinancePayoutPaidUseCase;
	}

	@GetMapping
	@RequireAdminPermission(AdminPermission.PAYOUT_SUPPORT_READ)
	public ResponseEntity<ApiResponse<AdminFinancePayoutResponse.ListPayload>> listPayoutRequests(
			@RequestParam(required = false) String status,
			@RequestParam(required = false) Integer page,
			@RequestParam(required = false) Integer limit,
			HttpServletRequest httpServletRequest
	) {
		AdminPayoutRequestListResult result = listAdminFinancePayoutRequestsUseCase.execute(
				new ListAdminFinancePayoutRequestsQuery(
						Optional.ofNullable(status),
						page,
						limit,
						resolveBearerToken(httpServletRequest)
				)
		);

		return ResponseEntity.ok(ApiResponse.success(
				HttpStatus.OK.value(),
				listAdminFinancePayoutRequestsUseCase.successMessage(),
				AdminFinancePayoutResponse.ListPayload.from(result)
		));
	}

	@PostMapping("/{payoutRequestId}/approve")
	@RequireAdminPermission(AdminPermission.PAYOUT_SUPPORT_APPROVE)
	public ResponseEntity<ApiResponse<AdminFinancePayoutResponse.Item>> approvePayoutRequest(
			@PathVariable UUID payoutRequestId,
			HttpServletRequest httpServletRequest
	) {
		AdminPayoutRequestItem result = approveAdminFinancePayoutUseCase.execute(
				new ApproveAdminFinancePayoutCommand(payoutRequestId, resolveBearerToken(httpServletRequest))
		);

		return ResponseEntity.ok(ApiResponse.success(
				HttpStatus.OK.value(),
				approveAdminFinancePayoutUseCase.successMessage(),
				AdminFinancePayoutResponse.Item.from(result)
		));
	}

	@PostMapping("/{payoutRequestId}/reject")
	@RequireAdminPermission(AdminPermission.PAYOUT_SUPPORT_APPROVE)
	public ResponseEntity<ApiResponse<AdminFinancePayoutResponse.Item>> rejectPayoutRequest(
			@PathVariable UUID payoutRequestId,
			@RequestBody(required = false) @Valid RejectAdminFinancePayoutRequest request,
			HttpServletRequest httpServletRequest
	) {
		String adminNote = request == null || request.adminNote() == null ? "" : request.adminNote();
		AdminPayoutRequestItem result = rejectAdminFinancePayoutUseCase.execute(
				new RejectAdminFinancePayoutCommand(
						payoutRequestId,
						adminNote,
						resolveBearerToken(httpServletRequest)
				)
		);

		return ResponseEntity.ok(ApiResponse.success(
				HttpStatus.OK.value(),
				rejectAdminFinancePayoutUseCase.successMessage(),
				AdminFinancePayoutResponse.Item.from(result)
		));
	}

	@PostMapping("/{payoutRequestId}/mark-paid")
	@RequireAdminPermission(AdminPermission.PAYOUT_SUPPORT_APPROVE)
	public ResponseEntity<ApiResponse<AdminFinancePayoutResponse.Item>> markPayoutRequestPaid(
			@PathVariable UUID payoutRequestId,
			@RequestBody @Valid MarkAdminFinancePayoutPaidRequest request,
			HttpServletRequest httpServletRequest
	) {
		AdminPayoutRequestItem result = markAdminFinancePayoutPaidUseCase.execute(
				new MarkAdminFinancePayoutPaidCommand(
						payoutRequestId,
						request.bankTransferRef(),
						resolveBearerToken(httpServletRequest)
				)
		);

		return ResponseEntity.ok(ApiResponse.success(
				HttpStatus.OK.value(),
				markAdminFinancePayoutPaidUseCase.successMessage(),
				AdminFinancePayoutResponse.Item.from(result)
		));
	}

	private String resolveBearerToken(HttpServletRequest request) {
		String authorization = request.getHeader("Authorization");
		if (authorization == null || !authorization.startsWith("Bearer ")) {
			return "";
		}
		return authorization.substring(7).trim();
	}
}
