package com.twohands.admin_service.unit.moderation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.twohands.admin_service.application.audit.AdminActionAuditLogger;
import com.twohands.admin_service.application.moderation.PostModerationOutboxPayloadBuilder;
import com.twohands.admin_service.application.moderation.restorepost.RestorePostCommand;
import com.twohands.admin_service.application.moderation.restorepost.RestorePostUseCase;
import com.twohands.admin_service.application.outbox.enqueue.InsertAdminOutboxEventCommand;
import com.twohands.admin_service.application.outbox.enqueue.InsertAdminOutboxEventUseCase;
import com.twohands.admin_service.constant.AdminPermission;
import com.twohands.admin_service.domain.auth.AdminAuthorizationService;
import com.twohands.admin_service.domain.integration.SocialPostGateway;
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

class RestorePostUseCaseTest {

	private static final String POST_ID = "507f1f77bcf86cd799439011";

	private final AdminAuthorizationService adminAuthorizationService = mock(AdminAuthorizationService.class);
	private final ContentModerationLogRepository contentModerationLogRepository = mock(ContentModerationLogRepository.class);
	private final SocialPostGateway socialPostGateway = mock(SocialPostGateway.class);
	private final InsertAdminOutboxEventUseCase insertAdminOutboxEventUseCase = mock(InsertAdminOutboxEventUseCase.class);
	private final AdminActionAuditLogger adminActionAuditLogger = mock(AdminActionAuditLogger.class);

	private RestorePostUseCase useCase;

	@BeforeEach
	void setUp() {
		useCase = new RestorePostUseCase(
				adminAuthorizationService,
				contentModerationLogRepository,
				socialPostGateway,
				insertAdminOutboxEventUseCase,
				new PostModerationOutboxPayloadBuilder(new ObjectMapper()),
				adminActionAuditLogger
		);
	}

	@Test
	void shouldRestorePostAndEnqueueOutbox() {
		UUID adminId = UUID.randomUUID();
		UUID outboxId = UUID.randomUUID();
		Instant now = Instant.now();
		UUID aggregateId = UUID.nameUUIDFromBytes(("admin:post:" + POST_ID).getBytes(StandardCharsets.UTF_8));

		when(adminAuthorizationService.requireCurrentAdminId()).thenReturn(adminId);
		when(socialPostGateway.isEnabled()).thenReturn(false);
		when(contentModerationLogRepository.save(any(ContentModerationLog.class))).thenAnswer(invocation -> {
			ContentModerationLog log = invocation.getArgument(0);
			assertThat(log.targetType()).isEqualTo(ContentModerationTargetType.POST);
			assertThat(log.action()).isEqualTo(ContentModerationAction.RESTORE);
			assertThat(log.targetId()).isEqualTo(POST_ID);
			assertThat(log.reason()).isEqualTo("Appeal approved");
			return log;
		});
		when(insertAdminOutboxEventUseCase.execute(any(InsertAdminOutboxEventCommand.class)))
				.thenReturn(new OutboxEvent(outboxId, "POST_RESTORED", aggregateId, "{}", OutboxStatus.PENDING, 0, now, null, null));

		var result = useCase.execute(new RestorePostCommand(POST_ID, "Appeal approved", null));

		assertThat(result.postId()).isEqualTo(POST_ID);
		assertThat(result.outboxEventId()).isEqualTo(outboxId);
		verify(adminAuthorizationService).requireAnyPermission(
				AdminPermission.POST_RESTORE,
				AdminPermission.POST_MODERATE
		);
		verify(insertAdminOutboxEventUseCase).execute(any(InsertAdminOutboxEventCommand.class));
	}

	@Test
	void shouldRejectBlankReason() {
		when(adminAuthorizationService.requireCurrentAdminId()).thenReturn(UUID.randomUUID());

		assertThatThrownBy(() -> useCase.execute(new RestorePostCommand(POST_ID, "  ", null)))
				.isInstanceOf(AppException.class)
				.extracting(ex -> ((AppException) ex).getErrorCode())
				.isEqualTo(ErrorCode.VALIDATION_ERROR);

		verify(contentModerationLogRepository, never()).save(any());
	}
}
