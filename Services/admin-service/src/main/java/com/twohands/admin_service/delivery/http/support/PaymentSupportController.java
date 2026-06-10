package com.twohands.admin_service.delivery.http.support;

import com.twohands.admin_service.application.support.viewpaymentdetail.ViewPaymentSupportDetailQuery;
import com.twohands.admin_service.application.support.viewpaymentdetail.ViewPaymentSupportDetailResult;
import com.twohands.admin_service.application.support.viewpaymentdetail.ViewPaymentSupportDetailUseCase;
import com.twohands.admin_service.application.support.viewpaymentsfor.ViewPaymentsForSupportQuery;
import com.twohands.admin_service.application.support.viewpaymentsfor.ViewPaymentsForSupportResult;
import com.twohands.admin_service.application.support.viewpaymentsfor.ViewPaymentsForSupportUseCase;
import com.twohands.admin_service.common.dto.ApiResponse;
import com.twohands.admin_service.constant.AdminPermission;
import com.twohands.admin_service.security.annotation.RequireAdminPermission;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/admin/api/v1/support/payments")
public class PaymentSupportController {

	private final ViewPaymentSupportDetailUseCase viewPaymentSupportDetailUseCase;
	private final ViewPaymentsForSupportUseCase viewPaymentsForSupportUseCase;

	public PaymentSupportController(
			ViewPaymentSupportDetailUseCase viewPaymentSupportDetailUseCase,
			ViewPaymentsForSupportUseCase viewPaymentsForSupportUseCase
	) {
		this.viewPaymentSupportDetailUseCase = viewPaymentSupportDetailUseCase;
		this.viewPaymentsForSupportUseCase = viewPaymentsForSupportUseCase;
	}

	@GetMapping
	@RequireAdminPermission(AdminPermission.PAYMENT_SUPPORT_READ)
	public ResponseEntity<ApiResponse<ViewPaymentsForSupportResponse>> listPaymentsForSupport(
			@RequestParam(required = false) String status,
			@RequestParam(name = "payment_method", required = false) String paymentMethod,
			@RequestParam(name = "order_id", required = false) String orderId,
			@RequestParam(required = false) String from,
			@RequestParam(required = false) String to,
			@RequestParam(required = false) Integer page,
			@RequestParam(required = false) Integer size,
			HttpServletRequest httpServletRequest
	) {
		ViewPaymentsForSupportResult result = viewPaymentsForSupportUseCase.execute(
				new ViewPaymentsForSupportQuery(
						status,
						paymentMethod,
						orderId,
						from,
						to,
						page,
						size,
						resolveBearerToken(httpServletRequest)
				)
		);

		return ResponseEntity.ok(ApiResponse.success(
				HttpStatus.OK.value(),
				viewPaymentsForSupportUseCase.successMessage(),
				ViewPaymentsForSupportResponse.from(
						result.page(),
						result.size(),
						result.totalElements(),
						result.totalPages(),
						result.payments()
				)
		));
	}

	@GetMapping("/{paymentId}")
	@RequireAdminPermission(AdminPermission.PAYMENT_SUPPORT_READ)
	public ResponseEntity<ApiResponse<ViewPaymentSupportDetailResponse>> viewPaymentSupportDetail(
			@PathVariable UUID paymentId,
			HttpServletRequest httpServletRequest
	) {
		ViewPaymentSupportDetailResult result = viewPaymentSupportDetailUseCase.execute(
				new ViewPaymentSupportDetailQuery(paymentId, resolveBearerToken(httpServletRequest))
		);

		return ResponseEntity.ok(ApiResponse.success(
				HttpStatus.OK.value(),
				viewPaymentSupportDetailUseCase.successMessage(),
				ViewPaymentSupportDetailResponse.from(result.detail())
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
