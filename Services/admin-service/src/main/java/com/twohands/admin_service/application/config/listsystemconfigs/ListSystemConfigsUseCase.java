package com.twohands.admin_service.application.config.listsystemconfigs;

import com.twohands.admin_service.constant.AdminPermission;
import com.twohands.admin_service.domain.auth.AdminAuthorizationService;
import com.twohands.admin_service.domain.common.PageRequest;
import com.twohands.admin_service.domain.common.PagedResult;
import com.twohands.admin_service.domain.common.PaginationPolicy;
import com.twohands.admin_service.domain.config.SystemConfigSearchCriteria;
import com.twohands.admin_service.domain.config.SystemConfigValueType;
import com.twohands.admin_service.exception.AppException;
import com.twohands.admin_service.exception.ErrorCode;
import com.twohands.admin_service.infrastructure.persistence.config.SystemConfigSearchRepository;
import com.twohands.admin_service.infrastructure.persistence.jpa.entity.SystemConfigEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ListSystemConfigsUseCase {

	private static final String SUCCESS_MESSAGE = "System configs retrieved successfully";

	private final AdminAuthorizationService adminAuthorizationService;
	private final SystemConfigSearchRepository systemConfigSearchRepository;

	public ListSystemConfigsUseCase(
			AdminAuthorizationService adminAuthorizationService,
			SystemConfigSearchRepository systemConfigSearchRepository
	) {
		this.adminAuthorizationService = adminAuthorizationService;
		this.systemConfigSearchRepository = systemConfigSearchRepository;
	}

	@Transactional(readOnly = true)
	public ListSystemConfigsResult execute(ListSystemConfigsQuery query) {
		adminAuthorizationService.requireCurrentAdminId();
		adminAuthorizationService.requirePermission(AdminPermission.SYSTEM_CONFIG_VIEW);

		SystemConfigSearchCriteria criteria = new SystemConfigSearchCriteria(
				normalizeOptionalText(query.query()),
				parseValueType(query.valueType()),
				query.active()
		);

		PageRequest pageRequest = PaginationPolicy.normalize(query.page(), query.size());
		PagedResult<SystemConfigEntity> page = systemConfigSearchRepository.search(criteria, pageRequest);

		List<SystemConfigListItem> items = page.items().stream()
				.map(this::toItem)
				.toList();

		return new ListSystemConfigsResult(
				page.page(),
				page.size(),
				page.totalElements(),
				page.totalPages(),
				items
		);
	}

	public String successMessage() {
		return SUCCESS_MESSAGE;
	}

	private SystemConfigListItem toItem(SystemConfigEntity entity) {
		return new SystemConfigListItem(
				entity.getId(),
				entity.getConfigKey(),
				entity.getConfigValue(),
				SystemConfigValueType.valueOf(entity.getValueType().name()),
				entity.getDescription(),
				entity.isActive(),
				entity.getCreatedBy(),
				entity.getCreatedAt(),
				entity.getUpdatedBy(),
				entity.getUpdatedAt()
		);
	}

	private String normalizeOptionalText(String value) {
		if (value == null) {
			return null;
		}
		String trimmed = value.trim();
		return trimmed.isEmpty() ? null : trimmed;
	}

	private SystemConfigValueType parseValueType(String valueType) {
		if (valueType == null || valueType.isBlank()) {
			return null;
		}
		try {
			return SystemConfigValueType.valueOf(valueType.trim().toUpperCase());
		} catch (IllegalArgumentException ex) {
			throw new AppException(ErrorCode.VALIDATION_ERROR, "Invalid value_type: " + valueType);
		}
	}
}
