package com.twohands.admin_service.integration.announcement;

import com.twohands.admin_service.application.announcement.createsystemannouncement.CreateSystemAnnouncementCommand;
import com.twohands.admin_service.application.announcement.createsystemannouncement.CreateSystemAnnouncementUseCase;
import com.twohands.admin_service.domain.announcement.SystemAnnouncementStatus;
import com.twohands.admin_service.domain.auth.AdminAuthorizationService;
import com.twohands.admin_service.infrastructure.persistence.jpa.enums.AnnouncementStatus;
import com.twohands.admin_service.infrastructure.persistence.jpa.repository.AdminActionLogJpaRepository;
import com.twohands.admin_service.infrastructure.persistence.jpa.repository.OutboxEventJpaRepository;
import com.twohands.admin_service.infrastructure.persistence.jpa.repository.SystemAnnouncementJpaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class CreateSystemAnnouncementIntegrationTest {

	@Autowired
	private CreateSystemAnnouncementUseCase createSystemAnnouncementUseCase;

	@Autowired
	private SystemAnnouncementJpaRepository systemAnnouncementJpaRepository;

	@Autowired
	private AdminActionLogJpaRepository adminActionLogJpaRepository;

	@Autowired
	private OutboxEventJpaRepository outboxEventJpaRepository;

	@MockBean
	private AdminAuthorizationService adminAuthorizationService;

	@Test
	void execute_persistsDraftAnnouncementAndAuditWithoutOutbox() {
		UUID adminId = UUID.randomUUID();
		when(adminAuthorizationService.requireCurrentAdminId()).thenReturn(adminId);

		var result = createSystemAnnouncementUseCase.execute(new CreateSystemAnnouncementCommand(
				"New checkout flow",
				"We are rolling out a new checkout experience.",
				"INFO",
				false,
				true
		));

		var entity = systemAnnouncementJpaRepository.findById(result.announcementId()).orElseThrow();
		assertEquals("New checkout flow", entity.getTitle());
		assertEquals(AnnouncementStatus.DRAFT, entity.getStatus());
		assertEquals(adminId, entity.getCreatedBy());
		assertNull(entity.getSentAt());
		assertEquals(SystemAnnouncementStatus.DRAFT, result.status());

		var auditLogs = adminActionLogJpaRepository.findAll();
		assertTrue(auditLogs.stream()
				.anyMatch(log -> "SYSTEM_ANNOUNCEMENT_CREATE".equals(log.getActionType().name())));

		assertEquals(0, outboxEventJpaRepository.count());
	}
}
