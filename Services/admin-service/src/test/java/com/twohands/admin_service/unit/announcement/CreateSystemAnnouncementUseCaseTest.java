package com.twohands.admin_service.unit.announcement;

import com.twohands.admin_service.application.announcement.createsystemannouncement.CreateSystemAnnouncementCommand;
import com.twohands.admin_service.application.announcement.createsystemannouncement.CreateSystemAnnouncementUseCase;
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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CreateSystemAnnouncementUseCaseTest {

	private final AdminAuthorizationService adminAuthorizationService = mock(AdminAuthorizationService.class);
	private final SystemAnnouncementRepository systemAnnouncementRepository = mock(SystemAnnouncementRepository.class);
	private final AdminActionAuditLogger adminActionAuditLogger = mock(AdminActionAuditLogger.class);

	private CreateSystemAnnouncementUseCase useCase;

	@BeforeEach
	void setUp() {
		useCase = new CreateSystemAnnouncementUseCase(
				adminAuthorizationService,
				systemAnnouncementRepository,
				adminActionAuditLogger
		);
	}

	@Test
	void shouldCreateDraftAnnouncement() {
		UUID adminId = UUID.randomUUID();
		when(adminAuthorizationService.requireCurrentAdminId()).thenReturn(adminId);
		when(systemAnnouncementRepository.save(any(SystemAnnouncement.class))).thenAnswer(invocation -> {
			SystemAnnouncement announcement = invocation.getArgument(0);
			assertThat(announcement.status()).isEqualTo(SystemAnnouncementStatus.DRAFT);
			assertThat(announcement.sentAt()).isNull();
			assertThat(announcement.createdBy()).isEqualTo(adminId);
			return announcement;
		});

		var result = useCase.execute(new CreateSystemAnnouncementCommand(
				"Platform maintenance",
				"Scheduled downtime tonight.",
				"CRITICAL",
				true,
				false
		));

		assertThat(result.status()).isEqualTo(SystemAnnouncementStatus.DRAFT);
		assertThat(result.severity()).isEqualTo(SystemAnnouncementSeverity.CRITICAL);
		assertThat(result.pinned()).isTrue();
		assertThat(result.dismissible()).isFalse();
		assertThat(result.createdBy()).isEqualTo(adminId);
		verify(adminAuthorizationService).requirePermission(AdminPermission.SYSTEM_ANNOUNCEMENT_CREATE);
	}

	@Test
	void shouldRejectInvalidSeverity() {
		when(adminAuthorizationService.requireCurrentAdminId()).thenReturn(UUID.randomUUID());

		assertThatThrownBy(() -> useCase.execute(new CreateSystemAnnouncementCommand(
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
}
