package com.twohands.admin_service.unit.announcement;

import com.twohands.admin_service.application.announcement.getsystemannouncement.GetSystemAnnouncementQuery;
import com.twohands.admin_service.application.announcement.getsystemannouncement.GetSystemAnnouncementUseCase;
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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class GetSystemAnnouncementUseCaseTest {

	private final AdminAuthorizationService adminAuthorizationService = mock(AdminAuthorizationService.class);
	private final SystemAnnouncementRepository systemAnnouncementRepository = mock(SystemAnnouncementRepository.class);

	private GetSystemAnnouncementUseCase useCase;

	@BeforeEach
	void setUp() {
		useCase = new GetSystemAnnouncementUseCase(
				adminAuthorizationService,
				systemAnnouncementRepository
		);
	}

	@Test
	void shouldReturnAnnouncementDetail() {
		UUID announcementId = UUID.randomUUID();
		UUID adminId = UUID.randomUUID();
		Instant now = Instant.now();

		SystemAnnouncement announcement = new SystemAnnouncement(
				announcementId,
				"Maintenance tonight",
				"Scheduled downtime.",
				SystemAnnouncementSeverity.WARNING,
				true,
				false,
				SystemAnnouncementStatus.DRAFT,
				adminId,
				now,
				null
		);

		when(adminAuthorizationService.requireCurrentAdminId()).thenReturn(adminId);
		when(systemAnnouncementRepository.findById(announcementId)).thenReturn(Optional.of(announcement));

		var result = useCase.execute(new GetSystemAnnouncementQuery(announcementId));

		assertThat(result.announcementId()).isEqualTo(announcementId);
		assertThat(result.title()).isEqualTo("Maintenance tonight");
		assertThat(result.severity()).isEqualTo(SystemAnnouncementSeverity.WARNING);
		assertThat(result.status()).isEqualTo(SystemAnnouncementStatus.DRAFT);
		assertThat(result.pinned()).isTrue();
		assertThat(result.dismissible()).isFalse();
		verify(adminAuthorizationService).requireAnyPermission(
				AdminPermission.SYSTEM_ANNOUNCEMENT_CREATE,
				AdminPermission.SYSTEM_ANNOUNCEMENT_UPDATE,
				AdminPermission.SYSTEM_ANNOUNCEMENT_PUBLISH,
				AdminPermission.SYSTEM_ANNOUNCEMENT_CANCEL
		);
	}

	@Test
	void shouldReturnNotFoundWhenAnnouncementMissing() {
		UUID announcementId = UUID.randomUUID();
		when(adminAuthorizationService.requireCurrentAdminId()).thenReturn(UUID.randomUUID());
		when(systemAnnouncementRepository.findById(announcementId)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> useCase.execute(new GetSystemAnnouncementQuery(announcementId)))
				.isInstanceOf(AppException.class)
				.extracting(ex -> ((AppException) ex).getErrorCode())
				.isEqualTo(ErrorCode.RESOURCE_NOT_FOUND);
	}
}
