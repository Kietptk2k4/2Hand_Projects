package com.twohands.admin_service.delivery.http.support;

import com.twohands.admin_service.application.support.vieworderdetail.ViewOrderSupportDetailQuery;
import com.twohands.admin_service.application.support.vieworderdetail.ViewOrderSupportDetailResult;
import com.twohands.admin_service.application.support.vieworderdetail.ViewOrderSupportDetailUseCase;
import com.twohands.admin_service.common.dto.ApiResponse;
import com.twohands.admin_service.constant.AdminPermission;
import com.twohands.admin_service.security.annotation.RequireAdminPermission;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/admin/api/v1/support/orders")
public class OrderSupportController {

	private final ViewOrderSupportDetailUseCase viewOrderSupportDetailUseCase;

	public OrderSupportController(ViewOrderSupportDetailUseCase viewOrderSupportDetailUseCase) {
		this.viewOrderSupportDetailUseCase = viewOrderSupportDetailUseCase;
	}

	@GetMapping("/{orderId}")
	@RequireAdminPermission(AdminPermission.ORDER_SUPPORT_READ)
	public ResponseEntity<ApiResponse<ViewOrderSupportDetailResponse>> viewOrderSupportDetail(
			@PathVariable UUID orderId,
			HttpServletRequest httpServletRequest
	) {
		ViewOrderSupportDetailResult result = viewOrderSupportDetailUseCase.execute(
				new ViewOrderSupportDetailQuery(orderId, resolveBearerToken(httpServletRequest))
		);

		ViewOrderSupportDetailResponse data = ViewOrderSupportDetailResponse.from(
				result.detail(),
				result.contactFieldsMasked()
		);

		return ResponseEntity.ok(ApiResponse.success(
				HttpStatus.OK.value(),
				viewOrderSupportDetailUseCase.successMessage(),
				data
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
