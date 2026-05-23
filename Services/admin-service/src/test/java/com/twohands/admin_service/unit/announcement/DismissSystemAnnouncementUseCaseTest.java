package com.twohands.admin_service.unit.announcement;

import com.twohands.admin_service.application.announcement.dismisssystemannouncement.DismissSystemAnnouncementCommand;
import com.twohands.admin_service.application.announcement.dismisssystemannouncement.DismissSystemAnnouncementUseCase;
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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DismissSystemAnnouncementUseCaseTest {

	private final AdminAuthorizationService adminAuthorizationService = mock(AdminAuthorizationService.class);
	private final SystemAnnouncementRepository systemAnnouncementRepository = mock(SystemAnnouncementRepository.class);

	private DismissSystemAnnouncementUseCase useCase;

	@BeforeEach
	void setUp() {
		useCase = new DismissSystemAnnouncementUseCase(adminAuthorizationService, systemAnnouncementRepository);
	}

	@Test
	void shouldAllowDismissForSentDismissibleAnnouncement() {
		UUID announcementId = UUID.randomUUID();
		SystemAnnouncement sent = announcement(announcementId, SystemAnnouncementStatus.SENT, true);

		when(adminAuthorizationService.requireCurrentAdminId()).thenReturn(UUID.randomUUID());
		when(systemAnnouncementRepository.findById(announcementId)).thenReturn(Optional.of(sent));

		var result = useCase.execute(new DismissSystemAnnouncementCommand(announcementId));

		assertThat(result.dismissible()).isTrue();
		assertThat(result.clientSidePersistence()).isTrue();
		verify(adminAuthorizationService).requireCurrentAdminId();
	}

	@Test
	void shouldRejectNonDismissibleAnnouncement() {
		UUID announcementId = UUID.randomUUID();
		when(adminAuthorizationService.requireCurrentAdminId()).thenReturn(UUID.randomUUID());
		when(systemAnnouncementRepository.findById(announcementId))
				.thenReturn(Optional.of(announcement(announcementId, SystemAnnouncementStatus.SENT, false)));

		assertThatThrownBy(() -> useCase.execute(new DismissSystemAnnouncementCommand(announcementId)))
				.isInstanceOf(AppException.class)
				.extracting(ex -> ((AppException) ex).getErrorCode())
				.isEqualTo(ErrorCode.SYSTEM_ANNOUNCEMENT_CONFLICT);
	}

	@Test
	void shouldRejectDraftAnnouncement() {
		UUID announcementId = UUID.randomUUID();
		when(adminAuthorizationService.requireCurrentAdminId()).thenReturn(UUID.randomUUID());
		when(systemAnnouncementRepository.findById(announcementId))
				.thenReturn(Optional.of(announcement(announcementId, SystemAnnouncementStatus.DRAFT, true)));

		assertThatThrownBy(() -> useCase.execute(new DismissSystemAnnouncementCommand(announcementId)))
				.isInstanceOf(AppException.class)
				.extracting(ex -> ((AppException) ex).getErrorCode())
				.isEqualTo(ErrorCode.SYSTEM_ANNOUNCEMENT_CONFLICT);
	}

	@Test
	void shouldReturnNotFoundWhenAnnouncementMissing() {
		UUID announcementId = UUID.randomUUID();
		when(adminAuthorizationService.requireCurrentAdminId()).thenReturn(UUID.randomUUID());
		when(systemAnnouncementRepository.findById(announcementId)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> useCase.execute(new DismissSystemAnnouncementCommand(announcementId)))
				.isInstanceOf(AppException.class)
				.extracting(ex -> ((AppException) ex).getErrorCode())
				.isEqualTo(ErrorCode.RESOURCE_NOT_FOUND);
	}

	private static SystemAnnouncement announcement(UUID id, SystemAnnouncementStatus status, boolean dismissible) {
		return new SystemAnnouncement(
				id,
				"Title",
				"Content",
				SystemAnnouncementSeverity.INFO,
				false,
				dismissible,
				status,
				UUID.randomUUID(),
				Instant.now(),
				status == SystemAnnouncementStatus.SENT ? Instant.now() : null
		);
	}
}
