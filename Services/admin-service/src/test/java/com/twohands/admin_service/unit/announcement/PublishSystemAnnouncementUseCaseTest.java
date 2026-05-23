package com.twohands.admin_service.unit.announcement;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.twohands.admin_service.application.announcement.SystemAnnouncementOutboxPayloadBuilder;
import com.twohands.admin_service.application.announcement.publishsystemannouncement.PublishSystemAnnouncementCommand;
import com.twohands.admin_service.application.announcement.publishsystemannouncement.PublishSystemAnnouncementUseCase;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PublishSystemAnnouncementUseCaseTest {

	private final AdminAuthorizationService adminAuthorizationService = mock(AdminAuthorizationService.class);
	private final SystemAnnouncementRepository systemAnnouncementRepository = mock(SystemAnnouncementRepository.class);
	private final InsertAdminOutboxEventUseCase insertAdminOutboxEventUseCase = mock(InsertAdminOutboxEventUseCase.class);
	private final AdminActionAuditLogger adminActionAuditLogger = mock(AdminActionAuditLogger.class);

	private PublishSystemAnnouncementUseCase useCase;

	@BeforeEach
	void setUp() {
		useCase = new PublishSystemAnnouncementUseCase(
				adminAuthorizationService,
				systemAnnouncementRepository,
				insertAdminOutboxEventUseCase,
				new SystemAnnouncementOutboxPayloadBuilder(new ObjectMapper()),
				adminActionAuditLogger
		);
	}

	@Test
	void shouldPublishDraftAnnouncement() {
		UUID adminId = UUID.randomUUID();
		UUID announcementId = UUID.randomUUID();
		Instant now = Instant.now();

		SystemAnnouncement draft = new SystemAnnouncement(
				announcementId,
				"Maintenance",
				"Downtime tonight",
				SystemAnnouncementSeverity.WARNING,
				true,
				true,
				SystemAnnouncementStatus.DRAFT,
				adminId,
				now,
				null
		);

		when(adminAuthorizationService.requireCurrentAdminId()).thenReturn(adminId);
		when(systemAnnouncementRepository.findById(announcementId)).thenReturn(Optional.of(draft));
		when(systemAnnouncementRepository.save(any(SystemAnnouncement.class))).thenAnswer(invocation -> {
			SystemAnnouncement saved = invocation.getArgument(0);
			assertThat(saved.status()).isEqualTo(SystemAnnouncementStatus.SENT);
			assertThat(saved.sentAt()).isNotNull();
			return saved;
		});
		when(insertAdminOutboxEventUseCase.execute(any(InsertAdminOutboxEventCommand.class)))
				.thenReturn(new OutboxEvent(
						UUID.randomUUID(),
						"SYSTEM_ANNOUNCEMENT_PUBLISHED",
						announcementId,
						"{}",
						OutboxStatus.PENDING,
						0,
						now,
						null,
						null
				));

		var result = useCase.execute(new PublishSystemAnnouncementCommand(announcementId));

		assertThat(result.status()).isEqualTo(SystemAnnouncementStatus.SENT);
		assertThat(result.sentAt()).isNotNull();
		assertThat(result.outboxEventId()).isNotNull();
		verify(adminAuthorizationService).requirePermission(AdminPermission.SYSTEM_ANNOUNCEMENT_PUBLISH);
	}

	@Test
	void shouldReturnNotFoundWhenAnnouncementMissing() {
		UUID announcementId = UUID.randomUUID();
		when(adminAuthorizationService.requireCurrentAdminId()).thenReturn(UUID.randomUUID());
		when(systemAnnouncementRepository.findById(announcementId)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> useCase.execute(new PublishSystemAnnouncementCommand(announcementId)))
				.isInstanceOf(AppException.class)
				.extracting(ex -> ((AppException) ex).getErrorCode())
				.isEqualTo(ErrorCode.RESOURCE_NOT_FOUND);
	}

	@Test
	void shouldRejectWhenAnnouncementAlreadySent() {
		UUID announcementId = UUID.randomUUID();
		Instant now = Instant.now();

		SystemAnnouncement sent = new SystemAnnouncement(
				announcementId,
				"Title",
				"Content",
				SystemAnnouncementSeverity.INFO,
				false,
				true,
				SystemAnnouncementStatus.SENT,
				UUID.randomUUID(),
				now,
				now
		);

		when(adminAuthorizationService.requireCurrentAdminId()).thenReturn(UUID.randomUUID());
		when(systemAnnouncementRepository.findById(announcementId)).thenReturn(Optional.of(sent));

		assertThatThrownBy(() -> useCase.execute(new PublishSystemAnnouncementCommand(announcementId)))
				.isInstanceOf(AppException.class)
				.extracting(ex -> ((AppException) ex).getErrorCode())
				.isEqualTo(ErrorCode.SYSTEM_ANNOUNCEMENT_CONFLICT);
	}
}
