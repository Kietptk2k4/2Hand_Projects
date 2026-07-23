package com.twohands.admin_service.delivery.http.support;

import com.twohands.admin_service.application.support.overrideshipmentstatus.AdminOverrideShipmentStatusCommand;
import com.twohands.admin_service.application.support.overrideshipmentstatus.AdminOverrideShipmentStatusUseCase;
import com.twohands.admin_service.application.support.viewshipmentsupportlist.ViewShipmentSupportListQuery;
import com.twohands.admin_service.application.support.viewshipmentsupportlist.ViewShipmentSupportListResult;
import com.twohands.admin_service.application.support.viewshipmentsupportlist.ViewShipmentSupportListUseCase;
import com.twohands.admin_service.application.support.viewshipmentdetail.ViewShipmentSupportDetailQuery;
import com.twohands.admin_service.application.support.viewshipmentdetail.ViewShipmentSupportDetailResult;
import com.twohands.admin_service.application.support.viewshipmentdetail.ViewShipmentSupportDetailUseCase;
import com.twohands.admin_service.common.dto.ApiResponse;
import com.twohands.admin_service.constant.AdminPermission;
import com.twohands.admin_service.domain.support.AdminOverrideShipmentStatusResult;
import com.twohands.admin_service.security.annotation.RequireAdminPermission;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/admin/api/v1/support/shipments")
public class ShipmentSupportController {

	private final ViewShipmentSupportDetailUseCase viewShipmentSupportDetailUseCase;
	private final ViewShipmentSupportListUseCase viewShipmentSupportListUseCase;
	private final AdminOverrideShipmentStatusUseCase adminOverrideShipmentStatusUseCase;

	public ShipmentSupportController(
			ViewShipmentSupportDetailUseCase viewShipmentSupportDetailUseCase,
			ViewShipmentSupportListUseCase viewShipmentSupportListUseCase,
			AdminOverrideShipmentStatusUseCase adminOverrideShipmentStatusUseCase
	) {
		this.viewShipmentSupportDetailUseCase = viewShipmentSupportDetailUseCase;
		this.viewShipmentSupportListUseCase = viewShipmentSupportListUseCase;
		this.adminOverrideShipmentStatusUseCase = adminOverrideShipmentStatusUseCase;
	}

	@GetMapping
	@RequireAdminPermission(AdminPermission.SHIPMENT_SUPPORT_READ)
	public ResponseEntity<ApiResponse<ViewShipmentSupportListResponse>> listShipmentSupport(
			@RequestParam(required = false) String status,
			@RequestParam(required = false) String carrier,
			@RequestParam(required = false) String sort,
			@RequestParam(required = false) String q,
			@RequestParam(name = "order_id", required = false) String orderId,
			@RequestParam(required = false) String from,
			@RequestParam(required = false) String to,
			@RequestParam(required = false) Integer page,
			@RequestParam(required = false) Integer size,
			HttpServletRequest httpServletRequest
	) {
		ViewShipmentSupportListResult result = viewShipmentSupportListUseCase.execute(
				new ViewShipmentSupportListQuery(
						status,
						carrier,
						sort,
						q,
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
				viewShipmentSupportListUseCase.successMessage(),
				ViewShipmentSupportListResponse.from(
						result.page(),
						result.size(),
						result.totalElements(),
						result.totalPages(),
						result.shipments()
				)
		));
	}

	@GetMapping("/{shipmentId}")
	@RequireAdminPermission(AdminPermission.SHIPMENT_SUPPORT_READ)
	public ResponseEntity<ApiResponse<ViewShipmentSupportDetailResponse>> viewShipmentSupportDetail(
			@PathVariable UUID shipmentId,
			HttpServletRequest httpServletRequest
	) {
		ViewShipmentSupportDetailResult result = viewShipmentSupportDetailUseCase.execute(
				new ViewShipmentSupportDetailQuery(shipmentId, resolveBearerToken(httpServletRequest))
		);

		return ResponseEntity.ok(ApiResponse.success(
				HttpStatus.OK.value(),
				viewShipmentSupportDetailUseCase.successMessage(),
				ViewShipmentSupportDetailResponse.from(result.detail(), result.contactFieldsMasked())
		));
	}

	@PatchMapping("/{shipmentId}/status")
	@RequireAdminPermission(AdminPermission.SHIPMENT_SUPPORT_WRITE)
	public ResponseEntity<ApiResponse<AdminOverrideShipmentStatusResponse>> overrideShipmentStatus(
			@PathVariable UUID shipmentId,
			@Valid @RequestBody AdminOverrideShipmentStatusRequest request,
			HttpServletRequest httpServletRequest
	) {
		AdminOverrideShipmentStatusResult result = adminOverrideShipmentStatusUseCase.execute(
				new AdminOverrideShipmentStatusCommand(
						shipmentId,
						request.status(),
						request.reason(),
						request.forceOrDefault(),
						resolveBearerToken(httpServletRequest)
				)
		);

		return ResponseEntity.ok(ApiResponse.success(
				HttpStatus.OK.value(),
				adminOverrideShipmentStatusUseCase.successMessage(result),
				AdminOverrideShipmentStatusResponse.from(result)
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
