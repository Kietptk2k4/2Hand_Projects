package com.twohands.admin_service.application.config.viewhistory;

import com.twohands.admin_service.constant.AdminPermission;
import com.twohands.admin_service.domain.auth.AdminAuthorizationService;
import com.twohands.admin_service.domain.common.PageRequest;
import com.twohands.admin_service.domain.common.PagedResult;
import com.twohands.admin_service.domain.common.PaginationPolicy;
import com.twohands.admin_service.domain.config.SystemConfig;
import com.twohands.admin_service.domain.config.SystemConfigHistory;
import com.twohands.admin_service.domain.config.SystemConfigHistoryRepository;
import com.twohands.admin_service.domain.config.SystemConfigPolicy;
import com.twohands.admin_service.domain.config.SystemConfigRepository;
import com.twohands.admin_service.exception.AppException;
import com.twohands.admin_service.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ViewSystemConfigHistoryUseCase {

	private static final String SUCCESS_MESSAGE = "System config history retrieved successfully";

	private final AdminAuthorizationService adminAuthorizationService;
	private final SystemConfigRepository systemConfigRepository;
	private final SystemConfigHistoryRepository systemConfigHistoryRepository;

	public ViewSystemConfigHistoryUseCase(
			AdminAuthorizationService adminAuthorizationService,
			SystemConfigRepository systemConfigRepository,
			SystemConfigHistoryRepository systemConfigHistoryRepository
	) {
		this.adminAuthorizationService = adminAuthorizationService;
		this.systemConfigRepository = systemConfigRepository;
		this.systemConfigHistoryRepository = systemConfigHistoryRepository;
	}

	@Transactional(readOnly = true)
	public ViewSystemConfigHistoryResult execute(ViewSystemConfigHistoryQuery query) {
		adminAuthorizationService.requireCurrentAdminId();
		adminAuthorizationService.requirePermission(AdminPermission.SYSTEM_CONFIG_VIEW);

		SystemConfig config = systemConfigRepository.findById(query.configId())
				.orElseThrow(() -> new AppException(
						ErrorCode.RESOURCE_NOT_FOUND,
						ErrorCode.RESOURCE_NOT_FOUND.defaultMessage()
				));

		PageRequest pageRequest = PaginationPolicy.normalize(query.page(), query.size());
		PagedResult<SystemConfigHistory> historyPage = systemConfigHistoryRepository.findByConfigKeyOrderByCreatedAtDesc(
				config.configKey(),
				pageRequest
		);

		boolean valuesMasked = SystemConfigPolicy.isSecretLikeKey(config.configKey());
		List<SystemConfigHistoryItem> items = historyPage.items().stream()
				.map(entry -> toItem(entry, valuesMasked))
				.toList();

		return new ViewSystemConfigHistoryResult(
				config.id(),
				config.configKey(),
				historyPage.page(),
				historyPage.size(),
				historyPage.totalElements(),
				historyPage.totalPages(),
				valuesMasked,
				items
		);
	}

	public String successMessage() {
		return SUCCESS_MESSAGE;
	}

	private SystemConfigHistoryItem toItem(SystemConfigHistory entry, boolean valuesMasked) {
		return new SystemConfigHistoryItem(
				entry.id(),
				entry.configKey(),
				SystemConfigPolicy.maskValueIfSecret(entry.configKey(), entry.oldValue()),
				SystemConfigPolicy.maskValueIfSecret(entry.configKey(), entry.newValue()),
				entry.changedBy(),
				entry.reason(),
				entry.createdAt(),
				valuesMasked
		);
	}
}
