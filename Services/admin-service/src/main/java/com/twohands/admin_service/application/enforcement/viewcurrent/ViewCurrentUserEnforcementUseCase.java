package com.twohands.admin_service.application.enforcement.viewcurrent;

import com.twohands.admin_service.constant.AdminPermission;
import com.twohands.admin_service.domain.auth.AdminAuthorizationService;
import com.twohands.admin_service.domain.enforcement.UserEnforcement;
import com.twohands.admin_service.domain.enforcement.UserEnforcementRepository;
import com.twohands.admin_service.domain.integration.AuthUserLookupGateway;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class ViewCurrentUserEnforcementUseCase {

	private static final String SUCCESS_MESSAGE = "Current user enforcements retrieved successfully";

	private final AdminAuthorizationService adminAuthorizationService;
	private final UserEnforcementRepository userEnforcementRepository;
	private final AuthUserLookupGateway authUserLookupGateway;

	public ViewCurrentUserEnforcementUseCase(
			AdminAuthorizationService adminAuthorizationService,
			UserEnforcementRepository userEnforcementRepository,
			AuthUserLookupGateway authUserLookupGateway
	) {
		this.adminAuthorizationService = adminAuthorizationService;
		this.userEnforcementRepository = userEnforcementRepository;
		this.authUserLookupGateway = authUserLookupGateway;
	}

	@Transactional(readOnly = true)
	public ViewCurrentUserEnforcementResult execute(ViewCurrentUserEnforcementQuery query) {
		adminAuthorizationService.requireCurrentAdminId();
		adminAuthorizationService.requirePermission(AdminPermission.USER_ENFORCEMENT_READ);

		if (authUserLookupGateway.isEnabled()) {
			authUserLookupGateway.ensureUserExists(query.userId());
		}

		Instant now = Instant.now();
		List<CurrentUserEnforcementItem> items = userEnforcementRepository.findAllActiveByUserId(query.userId())
				.stream()
				.map(enforcement -> toItem(enforcement, now))
				.toList();

		return new ViewCurrentUserEnforcementResult(query.userId(), items);
	}

	public String successMessage() {
		return SUCCESS_MESSAGE;
	}

	private CurrentUserEnforcementItem toItem(UserEnforcement enforcement, Instant now) {
		boolean possiblyExpired = enforcement.expiresAt() != null && !enforcement.expiresAt().isAfter(now);
		return new CurrentUserEnforcementItem(
				enforcement.id(),
				enforcement.userId(),
				enforcement.actionType(),
				enforcement.reasonCode(),
				enforcement.description(),
				enforcement.expiresAt(),
				enforcement.enforcedBy(),
				enforcement.createdAt(),
				possiblyExpired
		);
	}
}
