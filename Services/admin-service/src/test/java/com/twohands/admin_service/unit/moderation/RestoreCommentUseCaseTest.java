package com.twohands.admin_service.unit.moderation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.twohands.admin_service.application.audit.AdminActionAuditLogger;
import com.twohands.admin_service.application.moderation.CommentModerationOutboxPayloadBuilder;
import com.twohands.admin_service.application.moderation.restorecomment.RestoreCommentCommand;
import com.twohands.admin_service.application.moderation.restorecomment.RestoreCommentUseCase;
import com.twohands.admin_service.application.outbox.enqueue.InsertAdminOutboxEventCommand;
import com.twohands.admin_service.application.outbox.enqueue.InsertAdminOutboxEventUseCase;
import com.twohands.admin_service.constant.AdminPermission;
import com.twohands.admin_service.domain.auth.AdminAuthorizationService;
import com.twohands.admin_service.domain.integration.SocialCommentGateway;
import com.twohands.admin_service.domain.moderation.ContentModerationAction;
import com.twohands.admin_service.domain.moderation.ContentModerationLog;
import com.twohands.admin_service.domain.moderation.ContentModerationLogRepository;
import com.twohands.admin_service.domain.moderation.ContentModerationTargetType;
import com.twohands.admin_service.domain.outbox.OutboxEvent;
import com.twohands.admin_service.domain.outbox.OutboxStatus;
import com.twohands.admin_service.exception.AppException;
import com.twohands.admin_service.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RestoreCommentUseCaseTest {

	private static final String COMMENT_ID = "507f1f77bcf86cd799439012";

	private final AdminAuthorizationService adminAuthorizationService = mock(AdminAuthorizationService.class);
	private final ContentModerationLogRepository contentModerationLogRepository = mock(ContentModerationLogRepository.class);
	private final SocialCommentGateway socialCommentGateway = mock(SocialCommentGateway.class);
	private final InsertAdminOutboxEventUseCase insertAdminOutboxEventUseCase = mock(InsertAdminOutboxEventUseCase.class);
	private final AdminActionAuditLogger adminActionAuditLogger = mock(AdminActionAuditLogger.class);

	private RestoreCommentUseCase useCase;

	@BeforeEach
	void setUp() {
		useCase = new RestoreCommentUseCase(
				adminAuthorizationService,
				contentModerationLogRepository,
				socialCommentGateway,
				insertAdminOutboxEventUseCase,
				new CommentModerationOutboxPayloadBuilder(new ObjectMapper()),
				adminActionAuditLogger
		);
	}

	@Test
	void shouldRestoreCommentAndEnqueueOutbox() {
		UUID adminId = UUID.randomUUID();
		UUID outboxId = UUID.randomUUID();
		Instant now = Instant.now();
		UUID aggregateId = UUID.nameUUIDFromBytes(("admin:comment:" + COMMENT_ID).getBytes(StandardCharsets.UTF_8));

		when(adminAuthorizationService.requireCurrentAdminId()).thenReturn(adminId);
		when(socialCommentGateway.isEnabled()).thenReturn(false);
		when(contentModerationLogRepository.save(any(ContentModerationLog.class))).thenAnswer(invocation -> {
			ContentModerationLog log = invocation.getArgument(0);
			assertThat(log.targetType()).isEqualTo(ContentModerationTargetType.COMMENT);
			assertThat(log.action()).isEqualTo(ContentModerationAction.RESTORE);
			assertThat(log.targetId()).isEqualTo(COMMENT_ID);
			assertThat(log.reason()).isEqualTo("Appeal approved");
			return log;
		});
		when(insertAdminOutboxEventUseCase.execute(any(InsertAdminOutboxEventCommand.class)))
				.thenReturn(new OutboxEvent(outboxId, "COMMENT_RESTORED", aggregateId, "{}", OutboxStatus.PENDING, 0, now, null, null));

		var result = useCase.execute(new RestoreCommentCommand(COMMENT_ID, "Appeal approved", null));

		assertThat(result.commentId()).isEqualTo(COMMENT_ID);
		assertThat(result.outboxEventId()).isEqualTo(outboxId);
		verify(adminAuthorizationService).requireAnyPermission(
				AdminPermission.COMMENT_RESTORE,
				AdminPermission.COMMENT_MODERATE
		);
		verify(insertAdminOutboxEventUseCase).execute(any(InsertAdminOutboxEventCommand.class));
	}

	@Test
	void shouldRejectBlankReason() {
		when(adminAuthorizationService.requireCurrentAdminId()).thenReturn(UUID.randomUUID());

		assertThatThrownBy(() -> useCase.execute(new RestoreCommentCommand(COMMENT_ID, "  ", null)))
				.isInstanceOf(AppException.class)
				.extracting(ex -> ((AppException) ex).getErrorCode())
				.isEqualTo(ErrorCode.VALIDATION_ERROR);

		verify(contentModerationLogRepository, never()).save(any());
	}
}
