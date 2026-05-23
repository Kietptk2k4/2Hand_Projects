package com.twohands.admin_service.delivery.http.config;

import com.twohands.admin_service.application.config.createsystemconfig.CreateSystemConfigCommand;
import com.twohands.admin_service.application.config.createsystemconfig.CreateSystemConfigResult;
import com.twohands.admin_service.application.config.createsystemconfig.CreateSystemConfigUseCase;
import com.twohands.admin_service.common.dto.ApiResponse;
import com.twohands.admin_service.constant.AdminPermission;
import com.twohands.admin_service.security.annotation.RequireAdminPermission;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/api/v1/system-configs")
public class SystemConfigController {

	private final CreateSystemConfigUseCase createSystemConfigUseCase;

	public SystemConfigController(CreateSystemConfigUseCase createSystemConfigUseCase) {
		this.createSystemConfigUseCase = createSystemConfigUseCase;
	}

	@PostMapping
	@RequireAdminPermission(AdminPermission.SYSTEM_CONFIG_UPDATE)
	public ResponseEntity<ApiResponse<CreateSystemConfigResponse>> create(
			@Valid @RequestBody CreateSystemConfigRequest request
	) {
		CreateSystemConfigResult result = createSystemConfigUseCase.execute(new CreateSystemConfigCommand(
				request.configKey(),
				request.configValue(),
				request.valueType(),
				request.description(),
				request.active(),
				request.reason()
		));

		CreateSystemConfigResponse data = new CreateSystemConfigResponse(
				result.configId(),
				result.configKey(),
				result.configValue(),
				result.valueType().name(),
				result.description(),
				result.active(),
				result.createdBy(),
				result.createdAt(),
				result.historyId(),
				result.outboxEventId()
		);

		return ResponseEntity.status(HttpStatus.CREATED)
				.body(ApiResponse.success(HttpStatus.CREATED.value(), createSystemConfigUseCase.successMessage(), data));
	}
}
