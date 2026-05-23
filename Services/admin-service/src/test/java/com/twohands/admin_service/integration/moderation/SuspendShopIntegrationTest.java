package com.twohands.admin_service.integration.moderation;

import com.twohands.admin_service.application.moderation.suspendshop.SuspendShopCommand;
import com.twohands.admin_service.application.moderation.suspendshop.SuspendShopUseCase;
import com.twohands.admin_service.domain.auth.AdminAuthorizationService;
import com.twohands.admin_service.infrastructure.persistence.jpa.enums.ContentModerationAction;
import com.twohands.admin_service.infrastructure.persistence.jpa.enums.ContentModerationTargetType;
import com.twohands.admin_service.infrastructure.persistence.jpa.repository.AdminActionLogJpaRepository;
import com.twohands.admin_service.infrastructure.persistence.jpa.repository.ContentModerationLogJpaRepository;
import com.twohands.admin_service.infrastructure.persistence.jpa.repository.OutboxEventJpaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class SuspendShopIntegrationTest {

	@Autowired
	private SuspendShopUseCase suspendShopUseCase;

	@Autowired
	private ContentModerationLogJpaRepository contentModerationLogJpaRepository;

	@Autowired
	private OutboxEventJpaRepository outboxEventJpaRepository;

	@Autowired
	private AdminActionLogJpaRepository adminActionLogJpaRepository;

	@MockBean
	private AdminAuthorizationService adminAuthorizationService;

	@Test
	void execute_persistsModerationLogOutboxAndAudit() {
		UUID adminId = UUID.randomUUID();
		UUID shopId = UUID.randomUUID();

		when(adminAuthorizationService.requireCurrentAdminId()).thenReturn(adminId);

		var result = suspendShopUseCase.execute(new SuspendShopCommand(
				shopId,
				"Policy violation",
				"Integration test note"
		));

		var moderationLog = contentModerationLogJpaRepository.findById(result.moderationLogId()).orElseThrow();
		assertEquals(ContentModerationTargetType.SHOP, moderationLog.getTargetType());
		assertEquals(shopId.toString(), moderationLog.getTargetId());
		assertEquals(ContentModerationAction.SUSPEND, moderationLog.getAction());
		assertEquals("Policy violation", moderationLog.getReason());
		assertEquals(adminId, moderationLog.getAdminId());

		var outbox = outboxEventJpaRepository.findById(result.outboxEventId()).orElseThrow();
		assertEquals("SHOP_SUSPENDED", outbox.getEventType());
		assertEquals(shopId, outbox.getAggregateId());
		assertNotNull(outbox.getPayload());

		var auditLogs = adminActionLogJpaRepository.findAll();
		assertEquals(1, auditLogs.stream()
				.filter(log -> "SHOP_SUSPEND".equals(log.getActionType().name()))
				.count());
	}
}
