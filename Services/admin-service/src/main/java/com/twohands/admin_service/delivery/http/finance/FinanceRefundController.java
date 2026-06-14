package com.twohands.admin_service.delivery.http.finance;

import com.twohands.admin_service.application.refund.confirmrefundapproval.ConfirmAdminRefundApprovalCommand;
import com.twohands.admin_service.application.refund.confirmrefundapproval.ConfirmAdminRefundApprovalUseCase;
import com.twohands.admin_service.application.refund.listrefundapprovals.ListAdminRefundApprovalsQuery;
import com.twohands.admin_service.application.refund.listrefundapprovals.ListAdminRefundApprovalsUseCase;
import com.twohands.admin_service.application.refund.rejectrefundapproval.RejectAdminRefundApprovalCommand;
import com.twohands.admin_service.application.refund.rejectrefundapproval.RejectAdminRefundApprovalUseCase;
import com.twohands.admin_service.common.dto.ApiResponse;
import com.twohands.admin_service.constant.AdminPermission;
import com.twohands.admin_service.domain.refund.AdminRefundApprovalItem;
import com.twohands.admin_service.domain.refund.AdminRefundApprovalListResult;
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
@RequestMapping("/admin/api/v1/refund-approvals")
public class FinanceRefundController {

	private final ListAdminRefundApprovalsUseCase listAdminRefundApprovalsUseCase;
	private final ConfirmAdminRefundApprovalUseCase confirmAdminRefundApprovalUseCase;
	private final RejectAdminRefundApprovalUseCase rejectAdminRefundApprovalUseCase;

	public FinanceRefundController(
			ListAdminRefundApprovalsUseCase listAdminRefundApprovalsUseCase,
			ConfirmAdminRefundApprovalUseCase confirmAdminRefundApprovalUseCase,
			RejectAdminRefundApprovalUseCase rejectAdminRefundApprovalUseCase
	) {
		this.listAdminRefundApprovalsUseCase = listAdminRefundApprovalsUseCase;
		this.confirmAdminRefundApprovalUseCase = confirmAdminRefundApprovalUseCase;
		this.rejectAdminRefundApprovalUseCase = rejectAdminRefundApprovalUseCase;
	}

	@GetMapping
	@RequireAdminPermission(AdminPermission.REFUND_SUPPORT_READ)
	public ResponseEntity<ApiResponse<AdminFinanceRefundResponse.ListPayload>> listRefundApprovals(
			@RequestParam(required = false) String status,
			@RequestParam(required = false) Integer page,
			@RequestParam(required = false) Integer limit,
			HttpServletRequest httpServletRequest
	) {
		AdminRefundApprovalListResult result = listAdminRefundApprovalsUseCase.execute(
				new ListAdminRefundApprovalsQuery(
						Optional.ofNullable(status),
						page,
						limit,
						resolveBearerToken(httpServletRequest)
				)
		);

		return ResponseEntity.ok(ApiResponse.success(
				HttpStatus.OK.value(),
				listAdminRefundApprovalsUseCase.successMessage(),
				AdminFinanceRefundResponse.ListPayload.from(result)
		));
	}

	@PostMapping("/{refundRequestId}/confirm")
	@RequireAdminPermission(AdminPermission.REFUND_SUPPORT_APPROVE)
	public ResponseEntity<ApiResponse<AdminFinanceRefundResponse.Item>> confirmRefundApproval(
			@PathVariable UUID refundRequestId,
			@RequestBody(required = false) @Valid RejectAdminFinanceRefundRequest request,
			HttpServletRequest httpServletRequest
	) {
		String adminNote = request == null || request.adminNote() == null ? "" : request.adminNote();
		AdminRefundApprovalItem result = confirmAdminRefundApprovalUseCase.execute(
				new ConfirmAdminRefundApprovalCommand(
						refundRequestId,
						adminNote,
						resolveBearerToken(httpServletRequest)
				)
		);

		return ResponseEntity.ok(ApiResponse.success(
				HttpStatus.OK.value(),
				confirmAdminRefundApprovalUseCase.successMessage(),
				AdminFinanceRefundResponse.Item.from(result)
		));
	}

	@PostMapping("/{refundRequestId}/reject")
	@RequireAdminPermission(AdminPermission.REFUND_SUPPORT_APPROVE)
	public ResponseEntity<ApiResponse<AdminFinanceRefundResponse.Item>> rejectRefundApproval(
			@PathVariable UUID refundRequestId,
			@RequestBody(required = false) @Valid RejectAdminFinanceRefundRequest request,
			HttpServletRequest httpServletRequest
	) {
		String adminNote = request == null || request.adminNote() == null ? "" : request.adminNote();
		AdminRefundApprovalItem result = rejectAdminRefundApprovalUseCase.execute(
				new RejectAdminRefundApprovalCommand(
						refundRequestId,
						adminNote,
						resolveBearerToken(httpServletRequest)
				)
		);

		return ResponseEntity.ok(ApiResponse.success(
				HttpStatus.OK.value(),
				rejectAdminRefundApprovalUseCase.successMessage(),
				AdminFinanceRefundResponse.Item.from(result)
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
