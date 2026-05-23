package com.twohands.admin_service.integration.announcement;

import com.twohands.admin_service.application.announcement.createsystemannouncement.CreateSystemAnnouncementCommand;
import com.twohands.admin_service.application.announcement.createsystemannouncement.CreateSystemAnnouncementUseCase;
import com.twohands.admin_service.application.announcement.publishsystemannouncement.PublishSystemAnnouncementCommand;
import com.twohands.admin_service.application.announcement.publishsystemannouncement.PublishSystemAnnouncementUseCase;
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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class PublishSystemAnnouncementIntegrationTest {

	@Autowired
	private CreateSystemAnnouncementUseCase createSystemAnnouncementUseCase;

	@Autowired
	private PublishSystemAnnouncementUseCase publishSystemAnnouncementUseCase;

	@Autowired
	private SystemAnnouncementJpaRepository systemAnnouncementJpaRepository;

	@Autowired
	private OutboxEventJpaRepository outboxEventJpaRepository;

	@Autowired
	private AdminActionLogJpaRepository adminActionLogJpaRepository;

	@MockBean
	private AdminAuthorizationService adminAuthorizationService;

	@Test
	void execute_publishesDraftSetsSentAtAndOutbox() {
		UUID adminId = UUID.randomUUID();
		when(adminAuthorizationService.requireCurrentAdminId()).thenReturn(adminId);

		var created = createSystemAnnouncementUseCase.execute(new CreateSystemAnnouncementCommand(
				"Checkout update",
				"New checkout is live.",
				"INFO",
				false,
				true
		));

		var published = publishSystemAnnouncementUseCase.execute(
				new PublishSystemAnnouncementCommand(created.announcementId())
		);

		var entity = systemAnnouncementJpaRepository.findById(created.announcementId()).orElseThrow();
		assertEquals(AnnouncementStatus.SENT, entity.getStatus());
		assertNotNull(entity.getSentAt());
		assertEquals(published.sentAt(), entity.getSentAt());

		var outbox = outboxEventJpaRepository.findById(published.outboxEventId()).orElseThrow();
		assertEquals("SYSTEM_ANNOUNCEMENT_PUBLISHED", outbox.getEventType());
		assertEquals(created.announcementId(), outbox.getAggregateId());
		assertNotNull(outbox.getPayload());

		var auditLogs = adminActionLogJpaRepository.findAll();
		assertEquals(1, auditLogs.stream()
				.filter(log -> "SYSTEM_ANNOUNCEMENT_PUBLISH".equals(log.getActionType().name()))
				.count());
	}
}
