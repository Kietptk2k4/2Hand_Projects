package com.twohands.commerce_service.delivery.http.admin;

import com.twohands.commerce_service.application.order.viewordersupport.ViewOrderSupportDetailCommand;
import com.twohands.commerce_service.application.order.viewordersupport.ViewOrderSupportDetailUseCase;
import com.twohands.commerce_service.common.dto.ApiResponse;
import com.twohands.commerce_service.delivery.http.order.ViewOrderDetailResponse;
import com.twohands.commerce_service.delivery.http.order.ViewOrderDetailResponseFactory;
import com.twohands.commerce_service.domain.order.ViewOrderDetailResult;
import com.twohands.commerce_service.security.AuthenticatedUser;
import com.twohands.commerce_service.security.CommerceAdminAuthorization;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/commerce/api/v1/admin/support/orders")
public class AdminOrderSupportController {

	private final ViewOrderSupportDetailUseCase viewOrderSupportDetailUseCase;
	private final CommerceAdminAuthorization commerceAdminAuthorization;

	public AdminOrderSupportController(
			ViewOrderSupportDetailUseCase viewOrderSupportDetailUseCase,
			CommerceAdminAuthorization commerceAdminAuthorization
	) {
		this.viewOrderSupportDetailUseCase = viewOrderSupportDetailUseCase;
		this.commerceAdminAuthorization = commerceAdminAuthorization;
	}

	@GetMapping("/{orderId}")
	public ResponseEntity<ApiResponse<ViewOrderDetailResponse>> viewOrderSupportDetail(
			@PathVariable UUID orderId,
			Authentication authentication
	) {
		AuthenticatedUser admin = resolveAuthenticatedUser(authentication);
		commerceAdminAuthorization.requirePermission(
				admin,
				CommerceAdminAuthorization.PERMISSION_ORDER_SUPPORT_READ
		);

		ViewOrderDetailResult result = viewOrderSupportDetailUseCase.execute(new ViewOrderSupportDetailCommand(orderId));

		return ResponseEntity.ok(ApiResponse.success(
				HttpStatus.OK.value(),
				viewOrderSupportDetailUseCase.successMessage(),
				ViewOrderDetailResponseFactory.from(result)
		));
	}

	private AuthenticatedUser resolveAuthenticatedUser(Authentication authentication) {
		if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedUser user)) {
			throw new IllegalStateException("Authenticated admin user is required");
		}
		return user;
	}
}
