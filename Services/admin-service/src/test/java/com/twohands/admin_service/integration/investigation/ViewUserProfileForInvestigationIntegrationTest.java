package com.twohands.admin_service.integration.investigation;

import com.twohands.admin_service.application.investigation.viewprofile.ViewUserProfileForInvestigationQuery;
import com.twohands.admin_service.application.investigation.viewprofile.ViewUserProfileForInvestigationUseCase;
import com.twohands.admin_service.domain.auth.AdminAuthorizationService;
import com.twohands.admin_service.domain.integration.AuthUserInvestigationGateway;
import com.twohands.admin_service.domain.integration.InvestigationUserProfile;
import com.twohands.admin_service.infrastructure.persistence.jpa.entity.UserEnforcementEntity;
import com.twohands.admin_service.infrastructure.persistence.jpa.enums.UserEnforcementActionType;
import com.twohands.admin_service.infrastructure.persistence.jpa.enums.UserEnforcementStatus;
import com.twohands.admin_service.infrastructure.persistence.jpa.repository.UserEnforcementJpaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ViewUserProfileForInvestigationIntegrationTest {

	@Autowired
	private ViewUserProfileForInvestigationUseCase viewUserProfileForInvestigationUseCase;

	@Autowired
	private UserEnforcementJpaRepository userEnforcementJpaRepository;

	@MockBean
	private AdminAuthorizationService adminAuthorizationService;

	@MockBean
	private AuthUserInvestigationGateway authUserInvestigationGateway;

	@Test
	void execute_returnsProfileWithActiveEnforcementSummary() {
		UUID userId = UUID.randomUUID();
		Instant now = Instant.now();

		when(adminAuthorizationService.requireCurrentAdminId()).thenReturn(UUID.randomUUID());
		when(authUserInvestigationGateway.isEnabled()).thenReturn(true);
		when(authUserInvestigationGateway.fetchInvestigationProfile(userId, "test-token"))
				.thenReturn(new InvestigationUserProfile(
						userId,
						"investigation@example.com",
						"ACTIVE",
						true,
						false,
						now,
						now.minusSeconds(3600),
						"Support User",
						null,
						"Bio",
						null,
						false
				));

		UserEnforcementEntity enforcement = new UserEnforcementEntity();
		enforcement.setId(UUID.randomUUID());
		enforcement.setUserId(userId);
		enforcement.setActionType(UserEnforcementActionType.SUSPEND);
		enforcement.setReasonCode("ABUSE");
		enforcement.setDescription("Investigation test");
		enforcement.setEnforcedBy(UUID.randomUUID());
		enforcement.setStatus(UserEnforcementStatus.ACTIVE);
		enforcement.setCreatedAt(now);
		enforcement.setUpdatedAt(now);
		userEnforcementJpaRepository.save(enforcement);

		var result = viewUserProfileForInvestigationUseCase.execute(
				new ViewUserProfileForInvestigationQuery(userId, "test-token")
		);

		assertEquals("investigation@example.com", result.email());
		assertEquals(1, result.currentEnforcements().size());
		assertEquals("SUSPEND", result.currentEnforcements().get(0).actionType().name());
	}
}
