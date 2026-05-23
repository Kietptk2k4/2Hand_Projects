package com.twohands.admin_service.delivery.http.moderation;

import com.twohands.admin_service.application.moderation.removeproduct.RemoveProductCommand;
import com.twohands.admin_service.application.moderation.removeproduct.RemoveProductResult;
import com.twohands.admin_service.application.moderation.removeproduct.RemoveProductUseCase;
import com.twohands.admin_service.application.moderation.restoreproduct.RestoreProductCommand;
import com.twohands.admin_service.application.moderation.restoreproduct.RestoreProductResult;
import com.twohands.admin_service.application.moderation.restoreproduct.RestoreProductUseCase;
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
@RequestMapping("/admin/api/v1/products")
public class ProductModerationController {

	private final RemoveProductUseCase removeProductUseCase;
	private final RestoreProductUseCase restoreProductUseCase;

	public ProductModerationController(
			RemoveProductUseCase removeProductUseCase,
			RestoreProductUseCase restoreProductUseCase
	) {
		this.removeProductUseCase = removeProductUseCase;
		this.restoreProductUseCase = restoreProductUseCase;
	}

	@PostMapping("/{productId}/remove")
	@RequireAdminPermission(AdminPermission.PRODUCT_REMOVE)
	public ResponseEntity<ApiResponse<RemoveProductResponse>> remove(
			@PathVariable UUID productId,
			@Valid @RequestBody RemoveProductRequest request
	) {
		RemoveProductResult result = removeProductUseCase.execute(new RemoveProductCommand(
				productId,
				request.reason(),
				request.note()
		));

		RemoveProductResponse data = new RemoveProductResponse(
				result.productId(),
				result.moderationLogId(),
				result.reason(),
				result.note(),
				result.removedBy(),
				result.removedAt(),
				result.outboxEventId()
		);

		return ResponseEntity.status(HttpStatus.OK)
				.body(ApiResponse.success(HttpStatus.OK.value(), removeProductUseCase.successMessage(), data));
	}

	@PostMapping("/{productId}/restore")
	@RequireAdminPermission({AdminPermission.PRODUCT_RESTORE, AdminPermission.PRODUCT_REMOVE})
	public ResponseEntity<ApiResponse<RestoreProductResponse>> restore(
			@PathVariable UUID productId,
			@Valid @RequestBody RestoreProductRequest request
	) {
		RestoreProductResult result = restoreProductUseCase.execute(new RestoreProductCommand(
				productId,
				request.reason(),
				request.note()
		));

		RestoreProductResponse data = new RestoreProductResponse(
				result.productId(),
				result.moderationLogId(),
				result.reason(),
				result.note(),
				result.restoredBy(),
				result.restoredAt(),
				result.outboxEventId()
		);

		return ResponseEntity.status(HttpStatus.OK)
				.body(ApiResponse.success(HttpStatus.OK.value(), restoreProductUseCase.successMessage(), data));
	}
}
