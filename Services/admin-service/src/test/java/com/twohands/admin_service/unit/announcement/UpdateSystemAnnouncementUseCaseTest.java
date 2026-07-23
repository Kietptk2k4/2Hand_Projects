package com.twohands.admin_service.unit.announcement;

import com.twohands.admin_service.application.announcement.updatesystemannouncement.UpdateSystemAnnouncementCommand;
import com.twohands.admin_service.application.announcement.updatesystemannouncement.UpdateSystemAnnouncementUseCase;
import com.twohands.admin_service.application.audit.AdminActionAuditLogger;
import com.twohands.admin_service.constant.AdminPermission;
import com.twohands.admin_service.domain.announcement.SystemAnnouncement;
import com.twohands.admin_service.domain.announcement.SystemAnnouncementRepository;
import com.twohands.admin_service.domain.announcement.SystemAnnouncementSeverity;
import com.twohands.admin_service.domain.announcement.SystemAnnouncementStatus;
import com.twohands.admin_service.domain.auth.AdminAuthorizationService;
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

class UpdateSystemAnnouncementUseCaseTest {

	private final AdminAuthorizationService adminAuthorizationService = mock(AdminAuthorizationService.class);
	private final SystemAnnouncementRepository systemAnnouncementRepository = mock(SystemAnnouncementRepository.class);
	private final AdminActionAuditLogger adminActionAuditLogger = mock(AdminActionAuditLogger.class);

	private UpdateSystemAnnouncementUseCase useCase;

	@BeforeEach
	void setUp() {
		useCase = new UpdateSystemAnnouncementUseCase(
				adminAuthorizationService,
				systemAnnouncementRepository,
				adminActionAuditLogger
		);
	}

	@Test
	void shouldUpdateDraftAnnouncement() {
		UUID adminId = UUID.randomUUID();
		UUID announcementId = UUID.randomUUID();
		Instant now = Instant.now();

		SystemAnnouncement draft = announcement(announcementId, adminId, now, SystemAnnouncementStatus.DRAFT);

		when(adminAuthorizationService.requireCurrentAdminId()).thenReturn(adminId);
		when(systemAnnouncementRepository.findById(announcementId)).thenReturn(Optional.of(draft));
		when(systemAnnouncementRepository.save(any(SystemAnnouncement.class))).thenAnswer(invocation -> {
			SystemAnnouncement saved = invocation.getArgument(0);
			assertThat(saved.title()).isEqualTo("Updated title");
			assertThat(saved.content()).isEqualTo("Updated content");
			assertThat(saved.severity()).isEqualTo(SystemAnnouncementSeverity.CRITICAL);
			assertThat(saved.pinned()).isTrue();
			assertThat(saved.dismissible()).isFalse();
			assertThat(saved.status()).isEqualTo(SystemAnnouncementStatus.DRAFT);
			return saved;
		});

		var result = useCase.execute(new UpdateSystemAnnouncementCommand(
				announcementId,
				"Updated title",
				"Updated content",
				"CRITICAL",
				true,
				false
		));

		assertThat(result.title()).isEqualTo("Updated title");
		assertThat(result.severity()).isEqualTo(SystemAnnouncementSeverity.CRITICAL);
		assertThat(result.pinned()).isTrue();
		assertThat(result.dismissible()).isFalse();
		verify(adminAuthorizationService).requirePermission(AdminPermission.SYSTEM_ANNOUNCEMENT_UPDATE);
	}

	@Test
	void shouldRejectUpdateWhenNotDraft() {
		UUID announcementId = UUID.randomUUID();
		SystemAnnouncement sent = announcement(
				announcementId,
				UUID.randomUUID(),
				Instant.now(),
				SystemAnnouncementStatus.SENT
		);

		when(adminAuthorizationService.requireCurrentAdminId()).thenReturn(UUID.randomUUID());
		when(systemAnnouncementRepository.findById(announcementId)).thenReturn(Optional.of(sent));

		assertThatThrownBy(() -> useCase.execute(new UpdateSystemAnnouncementCommand(
				announcementId,
				"Title",
				"Content",
				"INFO",
				false,
				true
		)))
				.isInstanceOf(AppException.class)
				.extracting(ex -> ((AppException) ex).getErrorCode())
				.isEqualTo(ErrorCode.SYSTEM_ANNOUNCEMENT_CONFLICT);

		verify(systemAnnouncementRepository, never()).save(any());
	}

	@Test
	void shouldRejectInvalidSeverity() {
		UUID announcementId = UUID.randomUUID();
		SystemAnnouncement draft = announcement(
				announcementId,
				UUID.randomUUID(),
				Instant.now(),
				SystemAnnouncementStatus.DRAFT
		);

		when(adminAuthorizationService.requireCurrentAdminId()).thenReturn(UUID.randomUUID());
		when(systemAnnouncementRepository.findById(announcementId)).thenReturn(Optional.of(draft));

		assertThatThrownBy(() -> useCase.execute(new UpdateSystemAnnouncementCommand(
				announcementId,
				"Title",
				"Content",
				"UNKNOWN",
				false,
				true
		)))
				.isInstanceOf(AppException.class)
				.extracting(ex -> ((AppException) ex).getErrorCode())
				.isEqualTo(ErrorCode.VALIDATION_ERROR);
	}

	@Test
	void shouldReturnNotFoundWhenAnnouncementMissing() {
		UUID announcementId = UUID.randomUUID();
		when(adminAuthorizationService.requireCurrentAdminId()).thenReturn(UUID.randomUUID());
		when(systemAnnouncementRepository.findById(announcementId)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> useCase.execute(new UpdateSystemAnnouncementCommand(
				announcementId,
				"Title",
				"Content",
				"INFO",
				false,
				true
		)))
				.isInstanceOf(AppException.class)
				.extracting(ex -> ((AppException) ex).getErrorCode())
				.isEqualTo(ErrorCode.RESOURCE_NOT_FOUND);
	}

	private static SystemAnnouncement announcement(
			UUID id,
			UUID createdBy,
			Instant createdAt,
			SystemAnnouncementStatus status
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
				status == SystemAnnouncementStatus.SENT ? createdAt : null
		);
	}
}
