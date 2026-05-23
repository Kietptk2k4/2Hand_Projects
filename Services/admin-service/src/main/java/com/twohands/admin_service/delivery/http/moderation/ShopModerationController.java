package com.twohands.admin_service.delivery.http.moderation;

import com.twohands.admin_service.application.moderation.closeshop.CloseShopCommand;
import com.twohands.admin_service.application.moderation.closeshop.CloseShopResult;
import com.twohands.admin_service.application.moderation.closeshop.CloseShopUseCase;
import com.twohands.admin_service.application.moderation.reopenshop.ReopenShopCommand;
import com.twohands.admin_service.application.moderation.reopenshop.ReopenShopResult;
import com.twohands.admin_service.application.moderation.reopenshop.ReopenShopUseCase;
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
	private final CloseShopUseCase closeShopUseCase;
	private final ReopenShopUseCase reopenShopUseCase;

	public ShopModerationController(
			SuspendShopUseCase suspendShopUseCase,
			CloseShopUseCase closeShopUseCase,
			ReopenShopUseCase reopenShopUseCase
	) {
		this.suspendShopUseCase = suspendShopUseCase;
		this.closeShopUseCase = closeShopUseCase;
		this.reopenShopUseCase = reopenShopUseCase;
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

	@PostMapping("/{shopId}/close")
	@RequireAdminPermission(AdminPermission.SHOP_CLOSE)
	public ResponseEntity<ApiResponse<CloseShopResponse>> close(
			@PathVariable UUID shopId,
			@Valid @RequestBody CloseShopRequest request
	) {
		CloseShopResult result = closeShopUseCase.execute(new CloseShopCommand(
				shopId,
				request.reason(),
				request.note()
		));

		CloseShopResponse data = new CloseShopResponse(
				result.shopId(),
				result.moderationLogId(),
				result.reason(),
				result.note(),
				result.closedBy(),
				result.closedAt(),
				result.outboxEventId()
		);

		return ResponseEntity.status(HttpStatus.OK)
				.body(ApiResponse.success(HttpStatus.OK.value(), closeShopUseCase.successMessage(), data));
	}

	@PostMapping("/{shopId}/reopen")
	@RequireAdminPermission({AdminPermission.SHOP_RESTORE, AdminPermission.SHOP_SUSPEND})
	public ResponseEntity<ApiResponse<ReopenShopResponse>> reopen(
			@PathVariable UUID shopId,
			@Valid @RequestBody ReopenShopRequest request
	) {
		ReopenShopResult result = reopenShopUseCase.execute(new ReopenShopCommand(
				shopId,
				request.reason(),
				request.note()
		));

		ReopenShopResponse data = new ReopenShopResponse(
				result.shopId(),
				result.moderationLogId(),
				result.reason(),
				result.note(),
				result.reopenedBy(),
				result.reopenedAt(),
				result.outboxEventId()
		);

		return ResponseEntity.status(HttpStatus.OK)
				.body(ApiResponse.success(HttpStatus.OK.value(), reopenShopUseCase.successMessage(), data));
	}
}
