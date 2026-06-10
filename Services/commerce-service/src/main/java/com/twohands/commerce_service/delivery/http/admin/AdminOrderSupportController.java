package com.twohands.commerce_service.delivery.http.admin;

import com.twohands.commerce_service.application.order.viewordersforsupport.ViewOrdersForSupportQuery;
import com.twohands.commerce_service.application.order.viewordersforsupport.ViewOrdersForSupportResult;
import com.twohands.commerce_service.application.order.viewordersforsupport.ViewOrdersForSupportUseCase;
import com.twohands.commerce_service.application.order.viewordersupport.ViewOrderSupportDetailCommand;
import com.twohands.commerce_service.application.order.viewordersupport.ViewOrderSupportDetailUseCase;
import com.twohands.commerce_service.common.dto.ApiResponse;
import com.twohands.commerce_service.delivery.http.order.ViewOrderDetailResponse;
import com.twohands.commerce_service.delivery.http.order.ViewOrderDetailResponseFactory;
import com.twohands.commerce_service.delivery.http.order.ViewOrdersForSupportResponse;
import com.twohands.commerce_service.domain.order.ViewOrderDetailResult;
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
@RequestMapping("/commerce/api/v1/admin/support/orders")
public class AdminOrderSupportController {

	private final ViewOrderSupportDetailUseCase viewOrderSupportDetailUseCase;
	private final ViewOrdersForSupportUseCase viewOrdersForSupportUseCase;
	private final CommerceAdminAuthorization commerceAdminAuthorization;

	public AdminOrderSupportController(
			ViewOrderSupportDetailUseCase viewOrderSupportDetailUseCase,
			ViewOrdersForSupportUseCase viewOrdersForSupportUseCase,
			CommerceAdminAuthorization commerceAdminAuthorization
	) {
		this.viewOrderSupportDetailUseCase = viewOrderSupportDetailUseCase;
		this.viewOrdersForSupportUseCase = viewOrdersForSupportUseCase;
		this.commerceAdminAuthorization = commerceAdminAuthorization;
	}

	@GetMapping
	public ResponseEntity<ApiResponse<ViewOrdersForSupportResponse>> listOrdersForSupport(
			@RequestParam(required = false) String status,
			@RequestParam(name = "payment_method", required = false) String paymentMethod,
			@RequestParam(required = false) String from,
			@RequestParam(required = false) String to,
			@RequestParam(required = false) String sort,
			@RequestParam(required = false) Integer page,
			@RequestParam(required = false) Integer size,
			Authentication authentication
	) {
		AuthenticatedUser admin = resolveAuthenticatedUser(authentication);
		commerceAdminAuthorization.requirePermission(
				admin,
				CommerceAdminAuthorization.PERMISSION_ORDER_SUPPORT_READ
		);

		ViewOrdersForSupportResult result = viewOrdersForSupportUseCase.execute(
				new ViewOrdersForSupportQuery(status, paymentMethod, from, to, sort, page, size)
		);

		return ResponseEntity.ok(ApiResponse.success(
				HttpStatus.OK.value(),
				viewOrdersForSupportUseCase.successMessage(),
				ViewOrdersForSupportResponse.from(result)
		));
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
