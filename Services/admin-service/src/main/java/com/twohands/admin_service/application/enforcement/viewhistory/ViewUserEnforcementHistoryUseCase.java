package com.twohands.admin_service.application.enforcement.viewhistory;

import com.twohands.admin_service.constant.AdminPermission;
import com.twohands.admin_service.domain.auth.AdminAuthorizationService;
import com.twohands.admin_service.domain.common.PageRequest;
import com.twohands.admin_service.domain.common.PagedResult;
import com.twohands.admin_service.domain.common.PaginationPolicy;
import com.twohands.admin_service.domain.enforcement.UserEnforcement;
import com.twohands.admin_service.domain.enforcement.UserEnforcementLog;
import com.twohands.admin_service.domain.enforcement.UserEnforcementLogRepository;
import com.twohands.admin_service.domain.enforcement.UserEnforcementRepository;
import com.twohands.admin_service.domain.integration.AuthUserLookupGateway;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class ViewUserEnforcementHistoryUseCase {

	private static final String SUCCESS_MESSAGE = "User enforcement history retrieved successfully";
	private static final String ACTOR_ADMIN = "ADMIN";
	private static final String ACTOR_SYSTEM = "SYSTEM";

	private final AdminAuthorizationService adminAuthorizationService;
	private final UserEnforcementRepository userEnforcementRepository;
	private final UserEnforcementLogRepository userEnforcementLogRepository;
	private final AuthUserLookupGateway authUserLookupGateway;

	public ViewUserEnforcementHistoryUseCase(
			AdminAuthorizationService adminAuthorizationService,
			UserEnforcementRepository userEnforcementRepository,
			UserEnforcementLogRepository userEnforcementLogRepository,
			AuthUserLookupGateway authUserLookupGateway
	) {
		this.adminAuthorizationService = adminAuthorizationService;
		this.userEnforcementRepository = userEnforcementRepository;
		this.userEnforcementLogRepository = userEnforcementLogRepository;
		this.authUserLookupGateway = authUserLookupGateway;
	}

	@Transactional(readOnly = true)
	public ViewUserEnforcementHistoryResult execute(ViewUserEnforcementHistoryQuery query) {
		adminAuthorizationService.requireCurrentAdminId();
		adminAuthorizationService.requirePermission(AdminPermission.USER_ENFORCEMENT_READ);

		if (authUserLookupGateway.isEnabled()) {
			authUserLookupGateway.ensureUserExists(query.userId());
		}

		PageRequest pageRequest = PaginationPolicy.normalize(query.page(), query.size());
		PagedResult<UserEnforcement> enforcementPage = userEnforcementRepository.findAllByUserId(
				query.userId(),
				pageRequest
		);

		List<UUID> enforcementIds = enforcementPage.items().stream()
				.map(UserEnforcement::id)
				.toList();

		Map<UUID, List<UserEnforcementTransitionLogItem>> logsByEnforcementId = groupLogs(enforcementIds);

		List<UserEnforcementHistoryItem> items = enforcementPage.items().stream()
				.map(enforcement -> toHistoryItem(enforcement, logsByEnforcementId.getOrDefault(enforcement.id(), List.of())))
				.toList();

		return new ViewUserEnforcementHistoryResult(
				query.userId(),
				enforcementPage.page(),
				enforcementPage.size(),
				enforcementPage.totalElements(),
				enforcementPage.totalPages(),
				items
		);
	}

	public String successMessage() {
		return SUCCESS_MESSAGE;
	}

	private Map<UUID, List<UserEnforcementTransitionLogItem>> groupLogs(List<UUID> enforcementIds) {
		if (enforcementIds.isEmpty()) {
			return Map.of();
		}

		Map<UUID, List<UserEnforcementTransitionLogItem>> grouped = new LinkedHashMap<>();
		for (UserEnforcementLog log : userEnforcementLogRepository.findByEnforcementIdsOrderByCreatedAtDesc(enforcementIds)) {
			grouped.computeIfAbsent(log.enforcementId(), ignored -> new ArrayList<>())
					.add(toLogItem(log));
		}

		grouped.values().forEach(logs -> logs.sort(Comparator.comparing(UserEnforcementTransitionLogItem::createdAt).reversed()));
		return grouped;
	}

	private UserEnforcementHistoryItem toHistoryItem(
			UserEnforcement enforcement,
			List<UserEnforcementTransitionLogItem> logs
	) {
		return new UserEnforcementHistoryItem(
				enforcement.id(),
				enforcement.userId(),
				enforcement.actionType(),
				enforcement.reasonCode(),
				enforcement.description(),
				enforcement.expiresAt(),
				enforcement.enforcedBy(),
				enforcement.status(),
				enforcement.createdAt(),
				enforcement.updatedAt(),
				logs
		);
	}

	private UserEnforcementTransitionLogItem toLogItem(UserEnforcementLog log) {
		return new UserEnforcementTransitionLogItem(
				log.id(),
				log.oldStatus() == null ? null : log.oldStatus().name(),
				log.newStatus().name(),
				log.adminId(),
				log.adminId() == null ? ACTOR_SYSTEM : ACTOR_ADMIN,
				log.note(),
				log.createdAt()
		);
	}
}
