package com.twohands.admin_service.integration.announcement;

import com.twohands.admin_service.application.announcement.cancelsystemannouncement.CancelSystemAnnouncementCommand;
import com.twohands.admin_service.application.announcement.cancelsystemannouncement.CancelSystemAnnouncementUseCase;
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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class CancelSystemAnnouncementIntegrationTest {

	@Autowired
	private CreateSystemAnnouncementUseCase createSystemAnnouncementUseCase;

	@Autowired
	private PublishSystemAnnouncementUseCase publishSystemAnnouncementUseCase;

	@Autowired
	private CancelSystemAnnouncementUseCase cancelSystemAnnouncementUseCase;

	@Autowired
	private SystemAnnouncementJpaRepository systemAnnouncementJpaRepository;

	@Autowired
	private OutboxEventJpaRepository outboxEventJpaRepository;

	@Autowired
	private AdminActionLogJpaRepository adminActionLogJpaRepository;

	@MockBean
	private AdminAuthorizationService adminAuthorizationService;

	@Test
	void execute_cancelsDraftWithoutOutbox() {
		UUID adminId = UUID.randomUUID();
		when(adminAuthorizationService.requireCurrentAdminId()).thenReturn(adminId);

		var created = createSystemAnnouncementUseCase.execute(new CreateSystemAnnouncementCommand(
				"Old promo",
				"No longer valid.",
				"INFO",
				false,
				true
		));

		var cancelled = cancelSystemAnnouncementUseCase.execute(
				new CancelSystemAnnouncementCommand(created.announcementId())
		);

		var entity = systemAnnouncementJpaRepository.findById(created.announcementId()).orElseThrow();
		assertEquals(AnnouncementStatus.CANCELLED, entity.getStatus());
		assertNull(cancelled.outboxEventId());
		assertTrue(cancelled.stateChanged());

		assertTrue(adminActionLogJpaRepository.findAll().stream()
				.anyMatch(log -> "SYSTEM_ANNOUNCEMENT_CANCEL".equals(log.getActionType().name())));
	}

	@Test
	void execute_cancelsSentWithOutboxEvent() {
		UUID adminId = UUID.randomUUID();
		when(adminAuthorizationService.requireCurrentAdminId()).thenReturn(adminId);

		var created = createSystemAnnouncementUseCase.execute(new CreateSystemAnnouncementCommand(
				"Shipping delay",
				"Expect delays this week.",
				"WARNING",
				true,
				true
		));

		publishSystemAnnouncementUseCase.execute(new PublishSystemAnnouncementCommand(created.announcementId()));

		var cancelled = cancelSystemAnnouncementUseCase.execute(
				new CancelSystemAnnouncementCommand(created.announcementId())
		);

		var entity = systemAnnouncementJpaRepository.findById(created.announcementId()).orElseThrow();
		assertEquals(AnnouncementStatus.CANCELLED, entity.getStatus());
		assertNotNull(cancelled.outboxEventId());

		var outbox = outboxEventJpaRepository.findById(cancelled.outboxEventId()).orElseThrow();
		assertEquals("SYSTEM_ANNOUNCEMENT_CANCELLED", outbox.getEventType());
		assertEquals(created.announcementId(), outbox.getAggregateId());
	}
}
