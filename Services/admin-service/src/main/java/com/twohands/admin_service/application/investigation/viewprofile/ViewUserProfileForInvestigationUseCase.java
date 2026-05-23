package com.twohands.admin_service.application.investigation.viewprofile;

import com.twohands.admin_service.constant.AdminPermission;
import com.twohands.admin_service.domain.auth.AdminAuthorizationService;
import com.twohands.admin_service.domain.enforcement.UserEnforcement;
import com.twohands.admin_service.domain.enforcement.UserEnforcementRepository;
import com.twohands.admin_service.domain.integration.AuthUserInvestigationGateway;
import com.twohands.admin_service.domain.integration.InvestigationUserProfile;
import com.twohands.admin_service.exception.AppException;
import com.twohands.admin_service.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class ViewUserProfileForInvestigationUseCase {

	private static final String SUCCESS_MESSAGE = "User investigation profile retrieved successfully";

	private final AdminAuthorizationService adminAuthorizationService;
	private final AuthUserInvestigationGateway authUserInvestigationGateway;
	private final UserEnforcementRepository userEnforcementRepository;

	public ViewUserProfileForInvestigationUseCase(
			AdminAuthorizationService adminAuthorizationService,
			AuthUserInvestigationGateway authUserInvestigationGateway,
			UserEnforcementRepository userEnforcementRepository
	) {
		this.adminAuthorizationService = adminAuthorizationService;
		this.authUserInvestigationGateway = authUserInvestigationGateway;
		this.userEnforcementRepository = userEnforcementRepository;
	}

	@Transactional(readOnly = true)
	public ViewUserProfileForInvestigationResult execute(ViewUserProfileForInvestigationQuery query) {
		adminAuthorizationService.requireCurrentAdminId();
		adminAuthorizationService.requirePermission(AdminPermission.USER_INVESTIGATION_READ);

		if (!authUserInvestigationGateway.isEnabled()) {
			throw new AppException(
					ErrorCode.SERVICE_UNAVAILABLE,
					"Auth integration is disabled; user investigation profile is unavailable"
			);
		}

		InvestigationUserProfile profile = authUserInvestigationGateway.fetchInvestigationProfile(
				query.userId(),
				query.bearerToken()
		);

		Instant now = Instant.now();
		List<InvestigationEnforcementSummary> currentEnforcements = userEnforcementRepository
				.findAllActiveByUserId(query.userId())
				.stream()
				.map(enforcement -> toEnforcementSummary(enforcement, now))
				.toList();

		return new ViewUserProfileForInvestigationResult(
				profile.userId(),
				profile.email(),
				profile.status(),
				profile.emailVerified(),
				profile.phoneVerified(),
				profile.lastLoginAt(),
				profile.createdAt(),
				profile.displayName(),
				profile.avatarUrl(),
				profile.bio(),
				profile.website(),
				profile.isPrivate(),
				currentEnforcements
		);
	}

	public String successMessage() {
		return SUCCESS_MESSAGE;
	}

	private InvestigationEnforcementSummary toEnforcementSummary(UserEnforcement enforcement, Instant now) {
		boolean possiblyExpired = enforcement.expiresAt() != null && !enforcement.expiresAt().isAfter(now);
		return new InvestigationEnforcementSummary(
				enforcement.id(),
				enforcement.actionType(),
				enforcement.reasonCode(),
				enforcement.status().name(),
				enforcement.expiresAt(),
				possiblyExpired
		);
	}
}
