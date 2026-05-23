package com.twohands.admin_service.unit.announcement;

import com.twohands.admin_service.application.announcement.pinsystemannouncement.PinSystemAnnouncementCommand;
import com.twohands.admin_service.application.announcement.pinsystemannouncement.PinSystemAnnouncementUseCase;
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

class PinSystemAnnouncementUseCaseTest {

	private final AdminAuthorizationService adminAuthorizationService = mock(AdminAuthorizationService.class);
	private final SystemAnnouncementRepository systemAnnouncementRepository = mock(SystemAnnouncementRepository.class);
	private final AdminActionAuditLogger adminActionAuditLogger = mock(AdminActionAuditLogger.class);

	private PinSystemAnnouncementUseCase useCase;

	@BeforeEach
	void setUp() {
		useCase = new PinSystemAnnouncementUseCase(
				adminAuthorizationService,
				systemAnnouncementRepository,
				adminActionAuditLogger
		);
	}

	@Test
	void shouldPinDraftAnnouncement() {
		UUID adminId = UUID.randomUUID();
		UUID announcementId = UUID.randomUUID();
		Instant now = Instant.now();

		SystemAnnouncement draft = announcement(announcementId, adminId, now, false, SystemAnnouncementStatus.DRAFT);

		when(adminAuthorizationService.requireCurrentAdminId()).thenReturn(adminId);
		when(systemAnnouncementRepository.findById(announcementId)).thenReturn(Optional.of(draft));
		when(systemAnnouncementRepository.save(any(SystemAnnouncement.class))).thenAnswer(invocation -> {
			SystemAnnouncement saved = invocation.getArgument(0);
			assertThat(saved.pinned()).isTrue();
			return saved;
		});

		var result = useCase.execute(new PinSystemAnnouncementCommand(announcementId, true));

		assertThat(result.pinned()).isTrue();
		assertThat(result.stateChanged()).isTrue();
		verify(adminAuthorizationService).requirePermission(AdminPermission.SYSTEM_ANNOUNCEMENT_UPDATE);
	}

	@Test
	void shouldBeIdempotentWhenPinnedUnchanged() {
		UUID announcementId = UUID.randomUUID();
		SystemAnnouncement pinned = announcement(
				announcementId,
				UUID.randomUUID(),
				Instant.now(),
				true,
				SystemAnnouncementStatus.SENT
		);

		when(adminAuthorizationService.requireCurrentAdminId()).thenReturn(UUID.randomUUID());
		when(systemAnnouncementRepository.findById(announcementId)).thenReturn(Optional.of(pinned));

		var result = useCase.execute(new PinSystemAnnouncementCommand(announcementId, true));

		assertThat(result.stateChanged()).isFalse();
		verify(systemAnnouncementRepository, never()).save(any());
	}

	@Test
	void shouldRejectCancelledAnnouncement() {
		UUID announcementId = UUID.randomUUID();
		SystemAnnouncement cancelled = announcement(
				announcementId,
				UUID.randomUUID(),
				Instant.now(),
				false,
				SystemAnnouncementStatus.CANCELLED
		);

		when(adminAuthorizationService.requireCurrentAdminId()).thenReturn(UUID.randomUUID());
		when(systemAnnouncementRepository.findById(announcementId)).thenReturn(Optional.of(cancelled));

		assertThatThrownBy(() -> useCase.execute(new PinSystemAnnouncementCommand(announcementId, true)))
				.isInstanceOf(AppException.class)
				.extracting(ex -> ((AppException) ex).getErrorCode())
				.isEqualTo(ErrorCode.SYSTEM_ANNOUNCEMENT_CONFLICT);
	}

	@Test
	void shouldReturnNotFoundWhenAnnouncementMissing() {
		UUID announcementId = UUID.randomUUID();
		when(adminAuthorizationService.requireCurrentAdminId()).thenReturn(UUID.randomUUID());
		when(systemAnnouncementRepository.findById(announcementId)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> useCase.execute(new PinSystemAnnouncementCommand(announcementId, false)))
				.isInstanceOf(AppException.class)
				.extracting(ex -> ((AppException) ex).getErrorCode())
				.isEqualTo(ErrorCode.RESOURCE_NOT_FOUND);
	}

	private static SystemAnnouncement announcement(
			UUID id,
			UUID createdBy,
			Instant createdAt,
			boolean pinned,
			SystemAnnouncementStatus status
	) {
		return new SystemAnnouncement(
				id,
				"Title",
				"Content",
				SystemAnnouncementSeverity.INFO,
				pinned,
				true,
				status,
				createdBy,
				createdAt,
				status == SystemAnnouncementStatus.SENT ? createdAt : null
		);
	}
}
