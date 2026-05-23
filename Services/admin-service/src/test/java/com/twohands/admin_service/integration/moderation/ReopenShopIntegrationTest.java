package com.twohands.admin_service.integration.moderation;

import com.twohands.admin_service.application.moderation.reopenshop.ReopenShopCommand;
import com.twohands.admin_service.application.moderation.reopenshop.ReopenShopUseCase;
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
class ReopenShopIntegrationTest {

	@Autowired
	private ReopenShopUseCase reopenShopUseCase;

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

		var result = reopenShopUseCase.execute(new ReopenShopCommand(
				shopId,
				"Appeal approved",
				"Integration test note"
		));

		var moderationLog = contentModerationLogJpaRepository.findById(result.moderationLogId()).orElseThrow();
		assertEquals(ContentModerationTargetType.SHOP, moderationLog.getTargetType());
		assertEquals(shopId.toString(), moderationLog.getTargetId());
		assertEquals(ContentModerationAction.RESTORE, moderationLog.getAction());
		assertEquals("Appeal approved", moderationLog.getReason());
		assertEquals(adminId, moderationLog.getAdminId());

		var outbox = outboxEventJpaRepository.findById(result.outboxEventId()).orElseThrow();
		assertEquals("SHOP_RESTORED", outbox.getEventType());
		assertEquals(shopId, outbox.getAggregateId());
		assertNotNull(outbox.getPayload());

		var auditLogs = adminActionLogJpaRepository.findAll();
		assertEquals(1, auditLogs.stream()
				.filter(log -> "SHOP_RESTORE".equals(log.getActionType().name()))
				.count());
	}
}
