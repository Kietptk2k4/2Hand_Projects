package com.twohands.admin_service.unit.moderation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.twohands.admin_service.application.audit.AdminActionAuditLogger;
import com.twohands.admin_service.application.moderation.ProductModerationOutboxPayloadBuilder;
import com.twohands.admin_service.application.moderation.removeproduct.RemoveProductCommand;
import com.twohands.admin_service.application.moderation.removeproduct.RemoveProductUseCase;
import com.twohands.admin_service.application.outbox.enqueue.InsertAdminOutboxEventCommand;
import com.twohands.admin_service.application.outbox.enqueue.InsertAdminOutboxEventUseCase;
import com.twohands.admin_service.constant.AdminPermission;
import com.twohands.admin_service.domain.auth.AdminAuthorizationService;
import com.twohands.admin_service.domain.integration.CommerceProductGateway;
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

class RemoveProductUseCaseTest {

	private final AdminAuthorizationService adminAuthorizationService = mock(AdminAuthorizationService.class);
	private final ContentModerationLogRepository contentModerationLogRepository = mock(ContentModerationLogRepository.class);
	private final CommerceProductGateway commerceProductGateway = mock(CommerceProductGateway.class);
	private final InsertAdminOutboxEventUseCase insertAdminOutboxEventUseCase = mock(InsertAdminOutboxEventUseCase.class);
	private final AdminActionAuditLogger adminActionAuditLogger = mock(AdminActionAuditLogger.class);

	private RemoveProductUseCase useCase;

	@BeforeEach
	void setUp() {
		useCase = new RemoveProductUseCase(
				adminAuthorizationService,
				contentModerationLogRepository,
				commerceProductGateway,
				insertAdminOutboxEventUseCase,
				new ProductModerationOutboxPayloadBuilder(new ObjectMapper()),
				adminActionAuditLogger
		);
	}

	@Test
	void shouldRemoveProductAndEnqueueOutbox() {
		UUID adminId = UUID.randomUUID();
		UUID productId = UUID.randomUUID();
		UUID outboxId = UUID.randomUUID();
		Instant now = Instant.now();

		when(adminAuthorizationService.requireCurrentAdminId()).thenReturn(adminId);
		when(commerceProductGateway.isEnabled()).thenReturn(false);
		when(contentModerationLogRepository.save(any(ContentModerationLog.class))).thenAnswer(invocation -> {
			ContentModerationLog log = invocation.getArgument(0);
			assertThat(log.targetType()).isEqualTo(ContentModerationTargetType.PRODUCT);
			assertThat(log.action()).isEqualTo(ContentModerationAction.REMOVE);
			assertThat(log.targetId()).isEqualTo(productId.toString());
			assertThat(log.reason()).isEqualTo("Policy violation");
			assertThat(log.note()).isEqualTo("Internal note");
			return log;
		});
		when(insertAdminOutboxEventUseCase.execute(any(InsertAdminOutboxEventCommand.class)))
				.thenReturn(new OutboxEvent(outboxId, "PRODUCT_REMOVED", productId, "{}", OutboxStatus.PENDING, 0, now, null, null));

		var result = useCase.execute(new RemoveProductCommand(productId, "Policy violation", "Internal note"));

		assertThat(result.productId()).isEqualTo(productId);
		assertThat(result.moderationLogId()).isNotNull();
		assertThat(result.outboxEventId()).isEqualTo(outboxId);
		verify(adminAuthorizationService).requirePermission(AdminPermission.PRODUCT_REMOVE);
		verify(commerceProductGateway, never()).ensureProductExists(any());
		verify(insertAdminOutboxEventUseCase).execute(any(InsertAdminOutboxEventCommand.class));
	}

	@Test
	void shouldValidateProductExistsWhenCommerceIntegrationEnabled() {
		UUID adminId = UUID.randomUUID();
		UUID productId = UUID.randomUUID();
		Instant now = Instant.now();

		when(adminAuthorizationService.requireCurrentAdminId()).thenReturn(adminId);
		when(commerceProductGateway.isEnabled()).thenReturn(true);
		when(contentModerationLogRepository.save(any(ContentModerationLog.class)))
				.thenAnswer(invocation -> invocation.getArgument(0));
		when(insertAdminOutboxEventUseCase.execute(any(InsertAdminOutboxEventCommand.class)))
				.thenReturn(new OutboxEvent(UUID.randomUUID(), "PRODUCT_REMOVED", productId, "{}", OutboxStatus.PENDING, 0, now, null, null));

		useCase.execute(new RemoveProductCommand(productId, "Policy violation", null));

		verify(commerceProductGateway).ensureProductExists(productId);
	}

	@Test
	void shouldRejectBlankReason() {
		UUID adminId = UUID.randomUUID();
		UUID productId = UUID.randomUUID();

		when(adminAuthorizationService.requireCurrentAdminId()).thenReturn(adminId);

		assertThatThrownBy(() -> useCase.execute(new RemoveProductCommand(productId, "  ", null)))
				.isInstanceOf(AppException.class)
				.extracting(ex -> ((AppException) ex).getErrorCode())
				.isEqualTo(ErrorCode.VALIDATION_ERROR);

		verify(contentModerationLogRepository, never()).save(any());
	}
}
