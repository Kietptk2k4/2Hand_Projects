package com.twohands.admin_service.integration.audit;

import com.twohands.admin_service.application.audit.logcriticalpayload.LogCriticalAdminActionPayloadCommand;
import com.twohands.admin_service.application.audit.logcriticalpayload.LogCriticalAdminActionPayloadUseCase;
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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class LogCriticalAdminActionPayloadIntegrationTest {

	@Autowired
	private LogCriticalAdminActionPayloadUseCase logCriticalAdminActionPayloadUseCase;

	@Autowired
	private AdminActionLogJpaRepository adminActionLogJpaRepository;

	@Test
	void execute_storesSanitizedBeforeAfterPayload() {
		UUID adminId = UUID.randomUUID();
		String configKey = "MAX_CART_ITEMS";

		var result = logCriticalAdminActionPayloadUseCase.execute(new LogCriticalAdminActionPayloadCommand(
				adminId,
				"SYSTEM_CONFIG_UPDATE",
				AdminActionTargetType.CONFIG,
				configKey,
				AdminActionStatus.SUCCESS,
				"Config updated",
				"Changed max cart items",
				Map.of("config_key", configKey, "config_value", "20"),
				Map.of("config_key", configKey, "config_value", "30"),
				Map.of("reason", "capacity planning"),
				Map.of("history_id", UUID.randomUUID().toString())
		));

		var entity = adminActionLogJpaRepository.findById(result.logId()).orElseThrow();
		assertNotNull(entity.getRequestPayload());
		assertTrue(entity.getRequestPayload().contains("\"before\""));
		assertTrue(entity.getRequestPayload().contains("\"after\""));
		assertTrue(entity.getRequestPayload().contains("capacity planning"));
		assertFalse(entity.getRequestPayload().contains("password"));
		assertNotNull(entity.getResponsePayload());
		assertTrue(entity.getResponsePayload().contains("result_summary"));
	}
}
