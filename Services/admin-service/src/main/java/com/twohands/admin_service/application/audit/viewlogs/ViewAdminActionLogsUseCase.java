package com.twohands.admin_service.application.audit.viewlogs;

import com.twohands.admin_service.application.audit.AdminActionLogResponseMapper;
import com.twohands.admin_service.constant.AdminPermission;
import com.twohands.admin_service.domain.audit.AdminActionLogQueryPolicy;
import com.twohands.admin_service.domain.audit.AdminActionLogRepository;
import com.twohands.admin_service.domain.audit.AdminActionLogSearchCriteria;
import com.twohands.admin_service.domain.auth.AdminAuthorizationService;
import com.twohands.admin_service.domain.common.PageRequest;
import com.twohands.admin_service.domain.common.PagedResult;
import com.twohands.admin_service.domain.common.PaginationPolicy;
import com.twohands.admin_service.domain.audit.AdminActionLog;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class ViewAdminActionLogsUseCase {

	private static final String SUCCESS_MESSAGE = "Admin action logs retrieved successfully";

	private final AdminAuthorizationService adminAuthorizationService;
	private final AdminActionLogRepository adminActionLogRepository;
	private final AdminActionLogResponseMapper adminActionLogResponseMapper;

	public ViewAdminActionLogsUseCase(
			AdminAuthorizationService adminAuthorizationService,
			AdminActionLogRepository adminActionLogRepository,
			AdminActionLogResponseMapper adminActionLogResponseMapper
	) {
		this.adminAuthorizationService = adminAuthorizationService;
		this.adminActionLogRepository = adminActionLogRepository;
		this.adminActionLogResponseMapper = adminActionLogResponseMapper;
	}

	@Transactional(readOnly = true)
	public ViewAdminActionLogsResult execute(ViewAdminActionLogsQuery query) {
		adminAuthorizationService.requireCurrentAdminId();
		adminAuthorizationService.requirePermission(AdminPermission.ADMIN_AUDIT_VIEW);

		Instant from = AdminActionLogQueryPolicy.parseInstant(query.from(), "from");
		Instant to = AdminActionLogQueryPolicy.parseInstant(query.to(), "to");
		AdminActionLogQueryPolicy.validateDateRange(from, to);

		AdminActionLogSearchCriteria criteria = new AdminActionLogSearchCriteria(
				query.adminId(),
				AdminActionLogQueryPolicy.normalizeActionType(query.action()),
				AdminActionLogQueryPolicy.normalizeTargetType(query.targetType()),
				AdminActionLogQueryPolicy.normalizeOptionalText(query.targetId()),
				AdminActionLogQueryPolicy.parseStatus(query.status()),
				from,
				to
		);

		PageRequest pageRequest = PaginationPolicy.normalize(query.page(), query.size());
		PagedResult<AdminActionLog> page = adminActionLogRepository.search(criteria, pageRequest);

		List<AdminActionLogItem> items = page.items().stream()
				.map(adminActionLogResponseMapper::toItem)
				.toList();

		return new ViewAdminActionLogsResult(
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
}
