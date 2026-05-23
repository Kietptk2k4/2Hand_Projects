package com.twohands.admin_service.integration.announcement;

import com.twohands.admin_service.application.announcement.createsystemannouncement.CreateSystemAnnouncementCommand;
import com.twohands.admin_service.application.announcement.createsystemannouncement.CreateSystemAnnouncementUseCase;
import com.twohands.admin_service.application.announcement.pinsystemannouncement.PinSystemAnnouncementCommand;
import com.twohands.admin_service.application.announcement.pinsystemannouncement.PinSystemAnnouncementUseCase;
import com.twohands.admin_service.domain.auth.AdminAuthorizationService;
import com.twohands.admin_service.infrastructure.persistence.jpa.repository.AdminActionLogJpaRepository;
import com.twohands.admin_service.infrastructure.persistence.jpa.repository.SystemAnnouncementJpaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class PinSystemAnnouncementIntegrationTest {

	@Autowired
	private CreateSystemAnnouncementUseCase createSystemAnnouncementUseCase;

	@Autowired
	private PinSystemAnnouncementUseCase pinSystemAnnouncementUseCase;

	@Autowired
	private SystemAnnouncementJpaRepository systemAnnouncementJpaRepository;

	@Autowired
	private AdminActionLogJpaRepository adminActionLogJpaRepository;

	@MockBean
	private AdminAuthorizationService adminAuthorizationService;

	@Test
	void execute_persistsPinnedFlagAndAuditLog() {
		UUID adminId = UUID.randomUUID();
		when(adminAuthorizationService.requireCurrentAdminId()).thenReturn(adminId);

		var created = createSystemAnnouncementUseCase.execute(new CreateSystemAnnouncementCommand(
				"Flash sale",
				"Limited time offer.",
				"WARNING",
				false,
				true
		));

		var pinned = pinSystemAnnouncementUseCase.execute(
				new PinSystemAnnouncementCommand(created.announcementId(), true)
		);

		var entity = systemAnnouncementJpaRepository.findById(created.announcementId()).orElseThrow();
		assertTrue(entity.isPinned());
		assertEquals(true, pinned.pinned());
		assertTrue(pinned.stateChanged());

		var auditLogs = adminActionLogJpaRepository.findAll();
		assertTrue(auditLogs.stream()
				.anyMatch(log -> "SYSTEM_ANNOUNCEMENT_PIN".equals(log.getActionType().name())));
	}
}
