package com.twohands.admin_service.integration.moderation;

import com.twohands.admin_service.application.moderation.removereview.RemoveReviewCommand;
import com.twohands.admin_service.application.moderation.removereview.RemoveReviewUseCase;
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
class RemoveReviewIntegrationTest {

	@Autowired
	private RemoveReviewUseCase removeReviewUseCase;

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
		UUID reviewId = UUID.randomUUID();

		when(adminAuthorizationService.requireCurrentAdminId()).thenReturn(adminId);

		var result = removeReviewUseCase.execute(new RemoveReviewCommand(
				reviewId,
				"Severe policy violation",
				"Integration test note"
		));

		var moderationLog = contentModerationLogJpaRepository.findById(result.moderationLogId()).orElseThrow();
		assertEquals(ContentModerationTargetType.REVIEW, moderationLog.getTargetType());
		assertEquals(reviewId.toString(), moderationLog.getTargetId());
		assertEquals(ContentModerationAction.REMOVE, moderationLog.getAction());
		assertEquals("Severe policy violation", moderationLog.getReason());
		assertEquals(adminId, moderationLog.getAdminId());

		var outbox = outboxEventJpaRepository.findById(result.outboxEventId()).orElseThrow();
		assertEquals("REVIEW_REMOVED", outbox.getEventType());
		assertEquals(reviewId, outbox.getAggregateId());
		assertNotNull(outbox.getPayload());

		var auditLogs = adminActionLogJpaRepository.findAll();
		assertEquals(1, auditLogs.stream()
				.filter(log -> "REVIEW_REMOVE".equals(log.getActionType().name()))
				.count());
	}
}
