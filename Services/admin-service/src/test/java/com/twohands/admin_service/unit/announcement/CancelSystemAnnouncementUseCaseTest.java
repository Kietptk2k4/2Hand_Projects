package com.twohands.admin_service.unit.announcement;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.twohands.admin_service.application.announcement.SystemAnnouncementOutboxPayloadBuilder;
import com.twohands.admin_service.application.announcement.cancelsystemannouncement.CancelSystemAnnouncementCommand;
import com.twohands.admin_service.application.announcement.cancelsystemannouncement.CancelSystemAnnouncementUseCase;
import com.twohands.admin_service.application.audit.AdminActionAuditLogger;
import com.twohands.admin_service.application.outbox.enqueue.InsertAdminOutboxEventCommand;
import com.twohands.admin_service.application.outbox.enqueue.InsertAdminOutboxEventUseCase;
import com.twohands.admin_service.constant.AdminPermission;
import com.twohands.admin_service.domain.announcement.SystemAnnouncement;
import com.twohands.admin_service.domain.announcement.SystemAnnouncementRepository;
import com.twohands.admin_service.domain.announcement.SystemAnnouncementSeverity;
import com.twohands.admin_service.domain.announcement.SystemAnnouncementStatus;
import com.twohands.admin_service.domain.auth.AdminAuthorizationService;
import com.twohands.admin_service.domain.outbox.OutboxEvent;
import com.twohands.admin_service.domain.outbox.OutboxStatus;
import com.twohands.admin_service.exception.AppException;
import com.twohands.admin_service.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CancelSystemAnnouncementUseCaseTest {

	private final AdminAuthorizationService adminAuthorizationService = mock(AdminAuthorizationService.class);
	private final SystemAnnouncementRepository systemAnnouncementRepository = mock(SystemAnnouncementRepository.class);
	private final InsertAdminOutboxEventUseCase insertAdminOutboxEventUseCase = mock(InsertAdminOutboxEventUseCase.class);
	private final AdminActionAuditLogger adminActionAuditLogger = mock(AdminActionAuditLogger.class);

	private CancelSystemAnnouncementUseCase useCase;

	@BeforeEach
	void setUp() {
		useCase = new CancelSystemAnnouncementUseCase(
				adminAuthorizationService,
				systemAnnouncementRepository,
				insertAdminOutboxEventUseCase,
				new SystemAnnouncementOutboxPayloadBuilder(new ObjectMapper()),
				adminActionAuditLogger
		);
	}

	@Test
	void shouldCancelDraftWithoutOutbox() {
		UUID adminId = UUID.randomUUID();
		UUID announcementId = UUID.randomUUID();
		Instant now = Instant.now();

		SystemAnnouncement draft = announcement(announcementId, adminId, now, SystemAnnouncementStatus.DRAFT, null);

		when(adminAuthorizationService.requireCurrentAdminId()).thenReturn(adminId);
		when(systemAnnouncementRepository.findById(announcementId)).thenReturn(Optional.of(draft));
		when(systemAnnouncementRepository.save(any(SystemAnnouncement.class))).thenAnswer(invocation -> {
			SystemAnnouncement saved = invocation.getArgument(0);
			assertThat(saved.status()).isEqualTo(SystemAnnouncementStatus.CANCELLED);
			return saved;
		});

		var result = useCase.execute(new CancelSystemAnnouncementCommand(announcementId));

		assertThat(result.status()).isEqualTo(SystemAnnouncementStatus.CANCELLED);
		assertThat(result.stateChanged()).isTrue();
		assertThat(result.outboxEventId()).isNull();
		verify(insertAdminOutboxEventUseCase, never()).execute(any());
		verify(adminAuthorizationService).requirePermission(AdminPermission.SYSTEM_ANNOUNCEMENT_CANCEL);
	}

	@Test
	void shouldCancelSentWithOutbox() {
		UUID announcementId = UUID.randomUUID();
		Instant now = Instant.now();
		SystemAnnouncement sent = announcement(announcementId, UUID.randomUUID(), now, SystemAnnouncementStatus.SENT, now);

		when(adminAuthorizationService.requireCurrentAdminId()).thenReturn(UUID.randomUUID());
		when(systemAnnouncementRepository.findById(announcementId)).thenReturn(Optional.of(sent));
		when(systemAnnouncementRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
		when(insertAdminOutboxEventUseCase.execute(any(InsertAdminOutboxEventCommand.class)))
				.thenReturn(new OutboxEvent(
						UUID.randomUUID(),
						"SYSTEM_ANNOUNCEMENT_CANCELLED",
						announcementId,
						"{}",
						OutboxStatus.PENDING,
						0,
						now,
						null,
						null
				));

		var result = useCase.execute(new CancelSystemAnnouncementCommand(announcementId));

		assertThat(result.outboxEventId()).isNotNull();
		verify(insertAdminOutboxEventUseCase).execute(any(InsertAdminOutboxEventCommand.class));
	}

	@Test
	void shouldBeIdempotentWhenAlreadyCancelled() {
		UUID announcementId = UUID.randomUUID();
		SystemAnnouncement cancelled = announcement(
				announcementId,
				UUID.randomUUID(),
				Instant.now(),
				SystemAnnouncementStatus.CANCELLED,
				null
		);

		when(adminAuthorizationService.requireCurrentAdminId()).thenReturn(UUID.randomUUID());
		when(systemAnnouncementRepository.findById(announcementId)).thenReturn(Optional.of(cancelled));

		var result = useCase.execute(new CancelSystemAnnouncementCommand(announcementId));

		assertThat(result.stateChanged()).isFalse();
		verify(systemAnnouncementRepository, never()).save(any());
	}

	@Test
	void shouldReturnNotFoundWhenAnnouncementMissing() {
		UUID announcementId = UUID.randomUUID();
		when(adminAuthorizationService.requireCurrentAdminId()).thenReturn(UUID.randomUUID());
		when(systemAnnouncementRepository.findById(announcementId)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> useCase.execute(new CancelSystemAnnouncementCommand(announcementId)))
				.isInstanceOf(AppException.class)
				.extracting(ex -> ((AppException) ex).getErrorCode())
				.isEqualTo(ErrorCode.RESOURCE_NOT_FOUND);
	}

	private static SystemAnnouncement announcement(
			UUID id,
			UUID createdBy,
			Instant createdAt,
			SystemAnnouncementStatus status,
			Instant sentAt
	) {
		return new SystemAnnouncement(
				id,
				"Title",
				"Content",
				SystemAnnouncementSeverity.INFO,
				false,
				true,
				status,
				createdBy,
				createdAt,
				sentAt
		);
	}
}
