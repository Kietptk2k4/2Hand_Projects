package com.twohands.admin_service.unit.moderation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.twohands.admin_service.application.audit.AdminActionAuditLogger;
import com.twohands.admin_service.application.moderation.ShopModerationOutboxPayloadBuilder;
import com.twohands.admin_service.application.moderation.reopenshop.ReopenShopCommand;
import com.twohands.admin_service.application.moderation.reopenshop.ReopenShopUseCase;
import com.twohands.admin_service.application.outbox.enqueue.InsertAdminOutboxEventCommand;
import com.twohands.admin_service.application.outbox.enqueue.InsertAdminOutboxEventUseCase;
import com.twohands.admin_service.constant.AdminPermission;
import com.twohands.admin_service.domain.auth.AdminAuthorizationService;
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

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ReopenShopUseCaseTest {

	private final AdminAuthorizationService adminAuthorizationService = mock(AdminAuthorizationService.class);
	private final ContentModerationLogRepository contentModerationLogRepository = mock(ContentModerationLogRepository.class);
	private final InsertAdminOutboxEventUseCase insertAdminOutboxEventUseCase = mock(InsertAdminOutboxEventUseCase.class);
	private final AdminActionAuditLogger adminActionAuditLogger = mock(AdminActionAuditLogger.class);

	private ReopenShopUseCase useCase;

	@BeforeEach
	void setUp() {
		useCase = new ReopenShopUseCase(
				adminAuthorizationService,
				contentModerationLogRepository,
				insertAdminOutboxEventUseCase,
				new ShopModerationOutboxPayloadBuilder(new ObjectMapper()),
				adminActionAuditLogger
		);
	}

	@Test
	void shouldReopenShopAndEnqueueOutbox() {
		UUID adminId = UUID.randomUUID();
		UUID shopId = UUID.randomUUID();
		UUID outboxId = UUID.randomUUID();
		Instant now = Instant.now();

		when(adminAuthorizationService.requireCurrentAdminId()).thenReturn(adminId);
		when(contentModerationLogRepository.save(any(ContentModerationLog.class))).thenAnswer(invocation -> {
			ContentModerationLog log = invocation.getArgument(0);
			assertThat(log.targetType()).isEqualTo(ContentModerationTargetType.SHOP);
			assertThat(log.action()).isEqualTo(ContentModerationAction.RESTORE);
			assertThat(log.targetId()).isEqualTo(shopId.toString());
			assertThat(log.reason()).isEqualTo("Appeal approved");
			return log;
		});
		when(insertAdminOutboxEventUseCase.execute(any(InsertAdminOutboxEventCommand.class)))
				.thenReturn(new OutboxEvent(outboxId, "SHOP_RESTORED", shopId, "{}", OutboxStatus.PENDING, 0, now, null, null));

		var result = useCase.execute(new ReopenShopCommand(shopId, "Appeal approved", null));

		assertThat(result.shopId()).isEqualTo(shopId);
		assertThat(result.outboxEventId()).isEqualTo(outboxId);
		verify(adminAuthorizationService).requireAnyPermission(
				AdminPermission.SHOP_RESTORE,
				AdminPermission.SHOP_SUSPEND
		);
		verify(insertAdminOutboxEventUseCase).execute(any(InsertAdminOutboxEventCommand.class));
	}

	@Test
	void shouldRejectBlankReason() {
		UUID adminId = UUID.randomUUID();
		UUID shopId = UUID.randomUUID();

		when(adminAuthorizationService.requireCurrentAdminId()).thenReturn(adminId);

		assertThatThrownBy(() -> useCase.execute(new ReopenShopCommand(shopId, "  ", null)))
				.isInstanceOf(AppException.class)
				.extracting(ex -> ((AppException) ex).getErrorCode())
				.isEqualTo(ErrorCode.VALIDATION_ERROR);

		verify(contentModerationLogRepository, never()).save(any());
	}
}
