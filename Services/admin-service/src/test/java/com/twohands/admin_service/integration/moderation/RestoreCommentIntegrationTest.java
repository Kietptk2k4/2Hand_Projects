package com.twohands.admin_service.integration.moderation;

import com.twohands.admin_service.application.moderation.moderatecomment.ModerateCommentCommand;
import com.twohands.admin_service.application.moderation.moderatecomment.ModerateCommentUseCase;
import com.twohands.admin_service.application.moderation.restorecomment.RestoreCommentCommand;
import com.twohands.admin_service.application.moderation.restorecomment.RestoreCommentUseCase;
import com.twohands.admin_service.domain.auth.AdminAuthorizationService;
import com.twohands.admin_service.domain.integration.SocialCommentGateway;
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
class RestoreCommentIntegrationTest {

	private static final String COMMENT_ID = "507f1f77bcf86cd799439012";

	@Autowired
	private ModerateCommentUseCase moderateCommentUseCase;

	@Autowired
	private RestoreCommentUseCase restoreCommentUseCase;

	@Autowired
	private ContentModerationLogJpaRepository contentModerationLogJpaRepository;

	@Autowired
	private OutboxEventJpaRepository outboxEventJpaRepository;

	@Autowired
	private AdminActionLogJpaRepository adminActionLogJpaRepository;

	@MockBean
	private AdminAuthorizationService adminAuthorizationService;

	@MockBean
	private SocialCommentGateway socialCommentGateway;

	@Test
	void execute_persistsRestoreLogOutboxAndAudit() {
		UUID adminId = UUID.randomUUID();

		when(adminAuthorizationService.requireCurrentAdminId()).thenReturn(adminId);
		when(socialCommentGateway.isEnabled()).thenReturn(false);

		moderateCommentUseCase.execute(new ModerateCommentCommand(
				COMMENT_ID,
				ContentModerationAction.HIDE,
				"Spam content",
				"Hide note"
		));

		var result = restoreCommentUseCase.execute(new RestoreCommentCommand(
				COMMENT_ID,
				"Appeal approved",
				"Restore note"
		));

		var moderationLog = contentModerationLogJpaRepository.findById(result.moderationLogId()).orElseThrow();
		assertEquals(ContentModerationTargetType.COMMENT, moderationLog.getTargetType());
		assertEquals(COMMENT_ID, moderationLog.getTargetId());
		assertEquals(com.twohands.admin_service.infrastructure.persistence.jpa.enums.ContentModerationAction.RESTORE,
				moderationLog.getAction());
		assertEquals("Appeal approved", moderationLog.getReason());

		var outbox = outboxEventJpaRepository.findById(result.outboxEventId()).orElseThrow();
		assertEquals("COMMENT_RESTORED", outbox.getEventType());
		assertNotNull(outbox.getAggregateId());
		assertNotNull(outbox.getPayload());

		var auditLogs = adminActionLogJpaRepository.findAll();
		assertEquals(1, auditLogs.stream()
				.filter(log -> "COMMENT_RESTORE".equals(log.getActionType().name()))
				.count());
	}
}
