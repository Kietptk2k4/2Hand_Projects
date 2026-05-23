package com.twohands.admin_service.application.audit.viewlogs;

import com.twohands.admin_service.application.audit.AdminActionLogResponseMapper;
import com.twohands.admin_service.constant.AdminPermission;
import com.twohands.admin_service.domain.audit.AdminActionLogRepository;
import com.twohands.admin_service.domain.auth.AdminAuthorizationService;
import com.twohands.admin_service.exception.AppException;
import com.twohands.admin_service.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ViewAdminActionLogDetailUseCase {

	private static final String SUCCESS_MESSAGE = "Admin action log retrieved successfully";

	private final AdminAuthorizationService adminAuthorizationService;
	private final AdminActionLogRepository adminActionLogRepository;
	private final AdminActionLogResponseMapper adminActionLogResponseMapper;

	public ViewAdminActionLogDetailUseCase(
			AdminAuthorizationService adminAuthorizationService,
			AdminActionLogRepository adminActionLogRepository,
			AdminActionLogResponseMapper adminActionLogResponseMapper
	) {
		this.adminAuthorizationService = adminAuthorizationService;
		this.adminActionLogRepository = adminActionLogRepository;
		this.adminActionLogResponseMapper = adminActionLogResponseMapper;
	}

	@Transactional(readOnly = true)
	public AdminActionLogItem execute(ViewAdminActionLogDetailQuery query) {
		adminAuthorizationService.requireCurrentAdminId();
		adminAuthorizationService.requirePermission(AdminPermission.ADMIN_AUDIT_VIEW);

		return adminActionLogRepository.findById(query.logId())
				.map(adminActionLogResponseMapper::toItem)
				.orElseThrow(() -> new AppException(
						ErrorCode.RESOURCE_NOT_FOUND,
						ErrorCode.RESOURCE_NOT_FOUND.defaultMessage()
				));
	}

	public String successMessage() {
		return SUCCESS_MESSAGE;
	}
}
