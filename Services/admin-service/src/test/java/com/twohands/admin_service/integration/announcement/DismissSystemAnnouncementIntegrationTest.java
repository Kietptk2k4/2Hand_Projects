package com.twohands.admin_service.integration.announcement;

import com.twohands.admin_service.application.announcement.createsystemannouncement.CreateSystemAnnouncementCommand;
import com.twohands.admin_service.application.announcement.createsystemannouncement.CreateSystemAnnouncementUseCase;
import com.twohands.admin_service.application.announcement.dismisssystemannouncement.DismissSystemAnnouncementCommand;
import com.twohands.admin_service.application.announcement.dismisssystemannouncement.DismissSystemAnnouncementUseCase;
import com.twohands.admin_service.application.announcement.publishsystemannouncement.PublishSystemAnnouncementCommand;
import com.twohands.admin_service.application.announcement.publishsystemannouncement.PublishSystemAnnouncementUseCase;
import com.twohands.admin_service.domain.auth.AdminAuthorizationService;
import com.twohands.admin_service.exception.AppException;
import com.twohands.admin_service.exception.ErrorCode;
import com.twohands.admin_service.infrastructure.persistence.jpa.repository.SystemAnnouncementJpaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class DismissSystemAnnouncementIntegrationTest {

	@Autowired
	private CreateSystemAnnouncementUseCase createSystemAnnouncementUseCase;

	@Autowired
	private PublishSystemAnnouncementUseCase publishSystemAnnouncementUseCase;

	@Autowired
	private DismissSystemAnnouncementUseCase dismissSystemAnnouncementUseCase;

	@Autowired
	private SystemAnnouncementJpaRepository systemAnnouncementJpaRepository;

	@MockBean
	private AdminAuthorizationService adminAuthorizationService;

	@Test
	void execute_validatesDismissWithoutDbChange() {
		UUID adminId = UUID.randomUUID();
		when(adminAuthorizationService.requireCurrentAdminId()).thenReturn(adminId);

		var created = createSystemAnnouncementUseCase.execute(new CreateSystemAnnouncementCommand(
				"Promo",
				"Limited offer",
				"INFO",
				false,
				true
		));
		publishSystemAnnouncementUseCase.execute(new PublishSystemAnnouncementCommand(created.announcementId()));

		var before = systemAnnouncementJpaRepository.findById(created.announcementId()).orElseThrow();

		var result = dismissSystemAnnouncementUseCase.execute(
				new DismissSystemAnnouncementCommand(created.announcementId())
		);

		var after = systemAnnouncementJpaRepository.findById(created.announcementId()).orElseThrow();
		assertEquals(before.getStatus(), after.getStatus());
		assertEquals(before.isDismissible(), after.isDismissible());
		assertTrue(result.clientSidePersistence());
	}

	@Test
	void execute_rejectsNonDismissibleAnnouncement() {
		when(adminAuthorizationService.requireCurrentAdminId()).thenReturn(UUID.randomUUID());

		var created = createSystemAnnouncementUseCase.execute(new CreateSystemAnnouncementCommand(
				"Critical alert",
				"Cannot dismiss",
				"CRITICAL",
				true,
				false
		));
		publishSystemAnnouncementUseCase.execute(new PublishSystemAnnouncementCommand(created.announcementId()));

		AppException ex = assertThrows(AppException.class, () -> dismissSystemAnnouncementUseCase.execute(
				new DismissSystemAnnouncementCommand(created.announcementId())
		));
		assertEquals(ErrorCode.SYSTEM_ANNOUNCEMENT_CONFLICT, ex.getErrorCode());
	}
}
