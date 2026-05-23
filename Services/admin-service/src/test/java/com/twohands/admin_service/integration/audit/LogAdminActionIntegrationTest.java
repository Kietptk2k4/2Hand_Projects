package com.twohands.admin_service.integration.audit;

import com.twohands.admin_service.application.audit.logadminaction.LogAdminActionCommand;
import com.twohands.admin_service.application.audit.logadminaction.LogAdminActionUseCase;
import com.twohands.admin_service.domain.audit.AdminActionStatus;
import com.twohands.admin_service.domain.audit.AdminActionTargetType;
import com.twohands.admin_service.infrastructure.persistence.jpa.repository.AdminActionLogJpaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class LogAdminActionIntegrationTest {

	@Autowired
	private LogAdminActionUseCase logAdminActionUseCase;

	@Autowired
	private AdminActionLogJpaRepository adminActionLogJpaRepository;

	@Test
	void execute_persistsAdminActionLog() {
		UUID adminId = UUID.randomUUID();
		String targetUserId = UUID.randomUUID().toString();

		var result = logAdminActionUseCase.execute(new LogAdminActionCommand(
				adminId,
				"USER_SUSPEND",
				AdminActionTargetType.USER,
				targetUserId,
				AdminActionStatus.SUCCESS,
				"Suspended user",
				Map.of("reason", "abuse", "password", "must-not-persist"),
				Map.of("enforcementId", UUID.randomUUID().toString()),
				"10.0.0.1",
				"integration-test",
				false
		));

		var entity = adminActionLogJpaRepository.findById(result.logId()).orElseThrow();
		assertEquals(adminId, entity.getAdminId());
		assertEquals("USER_SUSPEND", entity.getActionType().name());
		assertEquals(AdminActionTargetType.USER, entity.getTargetType());
		assertEquals(targetUserId, entity.getTargetId());
		assertNotNull(entity.getRequestPayload());
		assertTrue(entity.getRequestPayload().contains("abuse"));
		assertTrue(entity.getRequestPayload().contains("***REDACTED***"));
		assertNotNull(entity.getResponsePayload());
		assertTrue(entity.getResponsePayload().contains("\"status\":\"SUCCESS\""));
		assertEquals("10.0.0.1", entity.getIpAddress());
	}
}
