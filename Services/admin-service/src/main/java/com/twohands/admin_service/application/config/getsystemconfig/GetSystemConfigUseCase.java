package com.twohands.admin_service.application.config.getsystemconfig;

import com.twohands.admin_service.constant.AdminPermission;
import com.twohands.admin_service.domain.auth.AdminAuthorizationService;
import com.twohands.admin_service.domain.config.SystemConfig;
import com.twohands.admin_service.domain.config.SystemConfigPolicy;
import com.twohands.admin_service.domain.config.SystemConfigRepository;
import com.twohands.admin_service.exception.AppException;
import com.twohands.admin_service.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GetSystemConfigUseCase {

	private static final String SUCCESS_MESSAGE = "System config retrieved successfully";

	private final AdminAuthorizationService adminAuthorizationService;
	private final SystemConfigRepository systemConfigRepository;

	public GetSystemConfigUseCase(
			AdminAuthorizationService adminAuthorizationService,
			SystemConfigRepository systemConfigRepository
	) {
		this.adminAuthorizationService = adminAuthorizationService;
		this.systemConfigRepository = systemConfigRepository;
	}

	@Transactional(readOnly = true)
	public GetSystemConfigResult execute(GetSystemConfigQuery query) {
		adminAuthorizationService.requireCurrentAdminId();
		adminAuthorizationService.requirePermission(AdminPermission.SYSTEM_CONFIG_VIEW);

		SystemConfig config = systemConfigRepository.findById(query.configId())
				.orElseThrow(() -> new AppException(
						ErrorCode.RESOURCE_NOT_FOUND,
						ErrorCode.RESOURCE_NOT_FOUND.defaultMessage()
				));

		return toResult(config);
	}

	public String successMessage() {
		return SUCCESS_MESSAGE;
	}

	private GetSystemConfigResult toResult(SystemConfig config) {
		boolean valueMasked = SystemConfigPolicy.isSecretLikeKey(config.configKey());
		return new GetSystemConfigResult(
				config.id(),
				config.configKey(),
				SystemConfigPolicy.maskValueIfSecret(config.configKey(), config.configValue()),
				config.valueType(),
				config.description(),
				config.active(),
				config.createdBy(),
				config.createdAt(),
				config.updatedBy(),
				config.updatedAt(),
				valueMasked
		);
	}
}
