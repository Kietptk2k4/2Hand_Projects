package com.twohands.admin_service.integration.moderation;

import com.twohands.admin_service.application.moderation.moderatepost.ModeratePostCommand;
import com.twohands.admin_service.application.moderation.moderatepost.ModeratePostUseCase;
import com.twohands.admin_service.domain.auth.AdminAuthorizationService;
import com.twohands.admin_service.domain.integration.SocialPostGateway;
import com.twohands.admin_service.domain.moderation.ContentModerationAction;
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
class ModeratePostIntegrationTest {

	private static final String POST_ID = "507f1f77bcf86cd799439011";

	@Autowired
	private ModeratePostUseCase moderatePostUseCase;

	@Autowired
	private ContentModerationLogJpaRepository contentModerationLogJpaRepository;

	@Autowired
	private OutboxEventJpaRepository outboxEventJpaRepository;

	@Autowired
	private AdminActionLogJpaRepository adminActionLogJpaRepository;

	@MockBean
	private AdminAuthorizationService adminAuthorizationService;

	@MockBean
	private SocialPostGateway socialPostGateway;

	@Test
	void execute_persistsModerationLogOutboxAndAudit() {
		UUID adminId = UUID.randomUUID();

		when(adminAuthorizationService.requireCurrentAdminId()).thenReturn(adminId);
		when(socialPostGateway.isEnabled()).thenReturn(false);

		var result = moderatePostUseCase.execute(new ModeratePostCommand(
				POST_ID,
				ContentModerationAction.HIDE,
				"Spam content",
				"Integration test note"
		));

		var moderationLog = contentModerationLogJpaRepository.findById(result.moderationLogId()).orElseThrow();
		assertEquals(ContentModerationTargetType.POST, moderationLog.getTargetType());
		assertEquals(POST_ID, moderationLog.getTargetId());
		assertEquals(com.twohands.admin_service.infrastructure.persistence.jpa.enums.ContentModerationAction.HIDE,
				moderationLog.getAction());
		assertEquals("Spam content", moderationLog.getReason());
		assertEquals(adminId, moderationLog.getAdminId());

		var outbox = outboxEventJpaRepository.findById(result.outboxEventId()).orElseThrow();
		assertEquals("POST_MODERATED", outbox.getEventType());
		assertNotNull(outbox.getAggregateId());
		assertNotNull(outbox.getPayload());

		var auditLogs = adminActionLogJpaRepository.findAll();
		assertEquals(1, auditLogs.stream()
				.filter(log -> "POST_MODERATE".equals(log.getActionType().name()))
				.count());
	}
}
