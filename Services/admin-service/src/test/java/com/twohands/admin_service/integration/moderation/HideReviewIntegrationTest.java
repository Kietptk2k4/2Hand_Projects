package com.twohands.admin_service.integration.moderation;

import com.twohands.admin_service.application.moderation.hidereview.HideReviewCommand;
import com.twohands.admin_service.application.moderation.hidereview.HideReviewUseCase;
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
class HideReviewIntegrationTest {

	@Autowired
	private HideReviewUseCase hideReviewUseCase;

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

		var result = hideReviewUseCase.execute(new HideReviewCommand(
				reviewId,
				"Spam content",
				"Integration test note"
		));

		var moderationLog = contentModerationLogJpaRepository.findById(result.moderationLogId()).orElseThrow();
		assertEquals(ContentModerationTargetType.REVIEW, moderationLog.getTargetType());
		assertEquals(reviewId.toString(), moderationLog.getTargetId());
		assertEquals(ContentModerationAction.HIDE, moderationLog.getAction());
		assertEquals("Spam content", moderationLog.getReason());
		assertEquals(adminId, moderationLog.getAdminId());

		var outbox = outboxEventJpaRepository.findById(result.outboxEventId()).orElseThrow();
		assertEquals("REVIEW_HIDDEN", outbox.getEventType());
		assertEquals(reviewId, outbox.getAggregateId());
		assertNotNull(outbox.getPayload());

		var auditLogs = adminActionLogJpaRepository.findAll();
		assertEquals(1, auditLogs.stream()
				.filter(log -> "REVIEW_HIDE".equals(log.getActionType().name()))
				.count());
	}
}
