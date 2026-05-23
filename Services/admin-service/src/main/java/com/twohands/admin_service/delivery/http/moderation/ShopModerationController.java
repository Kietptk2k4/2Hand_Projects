package com.twohands.admin_service.delivery.http.moderation;

import com.twohands.admin_service.application.moderation.suspendshop.SuspendShopCommand;
import com.twohands.admin_service.application.moderation.suspendshop.SuspendShopResult;
import com.twohands.admin_service.application.moderation.suspendshop.SuspendShopUseCase;
import com.twohands.admin_service.common.dto.ApiResponse;
import com.twohands.admin_service.constant.AdminPermission;
import com.twohands.admin_service.security.annotation.RequireAdminPermission;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/admin/api/v1/shops")
public class ShopModerationController {

	private final SuspendShopUseCase suspendShopUseCase;

	public ShopModerationController(SuspendShopUseCase suspendShopUseCase) {
		this.suspendShopUseCase = suspendShopUseCase;
	}

	@PostMapping("/{shopId}/suspend")
	@RequireAdminPermission(AdminPermission.SHOP_SUSPEND)
	public ResponseEntity<ApiResponse<SuspendShopResponse>> suspend(
			@PathVariable UUID shopId,
			@Valid @RequestBody SuspendShopRequest request
	) {
		SuspendShopResult result = suspendShopUseCase.execute(new SuspendShopCommand(
				shopId,
				request.reason(),
				request.note()
		));

		SuspendShopResponse data = new SuspendShopResponse(
				result.shopId(),
				result.moderationLogId(),
				result.reason(),
				result.note(),
				result.suspendedBy(),
				result.suspendedAt(),
				result.outboxEventId()
		);

		return ResponseEntity.status(HttpStatus.OK)
				.body(ApiResponse.success(HttpStatus.OK.value(), suspendShopUseCase.successMessage(), data));
	}
}
