package com.twohands.admin_service.delivery.http.support;

import com.twohands.admin_service.application.support.viewshipmentdetail.ViewShipmentSupportDetailQuery;
import com.twohands.admin_service.application.support.viewshipmentdetail.ViewShipmentSupportDetailResult;
import com.twohands.admin_service.application.support.viewshipmentdetail.ViewShipmentSupportDetailUseCase;
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
@RequestMapping("/admin/api/v1/support/shipments")
public class ShipmentSupportController {

	private final ViewShipmentSupportDetailUseCase viewShipmentSupportDetailUseCase;

	public ShipmentSupportController(ViewShipmentSupportDetailUseCase viewShipmentSupportDetailUseCase) {
		this.viewShipmentSupportDetailUseCase = viewShipmentSupportDetailUseCase;
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

	private String resolveBearerToken(HttpServletRequest request) {
		String authorization = request.getHeader("Authorization");
		if (authorization == null || !authorization.startsWith("Bearer ")) {
			return "";
		}
		return authorization.substring(7).trim();
	}
}
