package com.twohands.notification_service.integration.handler;

import com.twohands.notification_service.application.email.AdminSystemAnnouncementPayloadNormalizer;
import com.twohands.notification_service.application.ingest.NotificationEventIngestCommand;
import com.twohands.notification_service.application.ingest.StoreNotificationEventUseCase;
import com.twohands.notification_service.application.worker.ProcessNotificationEventCommand;
import com.twohands.notification_service.application.worker.ProcessNotificationEventOutcome;
import com.twohands.notification_service.application.worker.ProcessNotificationEventUseCase;
import com.twohands.notification_service.domain.notificationevent.NotificationEvent;
import com.twohands.notification_service.domain.notificationevent.NotificationEventPayloadCodec;
import com.twohands.notification_service.domain.notificationevent.NotificationEventRepository;
import com.twohands.notification_service.domain.notificationevent.NotificationEventStatus;
import com.twohands.notification_service.domain.notificationevent.NotificationSourceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = "notification.integrations.email.enabled=false")
class SystemAnnouncementFanOutNotificationIntegrationTest {

    @Autowired
    private ProcessNotificationEventUseCase processNotificationEventUseCase;

    @Autowired
    private StoreNotificationEventUseCase storeNotificationEventUseCase;

    @Autowired
    private NotificationEventRepository notificationEventRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private AdminSystemAnnouncementPayloadNormalizer adminSystemAnnouncementPayloadNormalizer;

    @BeforeEach
    void cleanTables() {
        jdbcTemplate.execute("DELETE FROM user_notification_settings");
        jdbcTemplate.execute("DELETE FROM user_notifications");
        jdbcTemplate.execute("DELETE FROM notification_events");
    }

    @Test
    void process_fansOutInAppNotificationsForExplicitRecipients() {
        UUID recipientOne = UUID.randomUUID();
        UUID recipientTwo = UUID.randomUUID();
        UUID announcementId = UUID.randomUUID();
        UUID eventId = ingestAnnouncementEvent(announcementId, recipientOne, recipientTwo, "INFO", true);

        var result = processNotificationEventUseCase.execute(new ProcessNotificationEventCommand(eventId));

        assertEquals(ProcessNotificationEventOutcome.COMPLETED, result.outcome());
        assertEquals(1, countNotifications(eventId, recipientOne));
        assertEquals(1, countNotifications(eventId, recipientTwo));
        assertEquals("SYSTEM_ANNOUNCEMENT", queryReferenceType(eventId, recipientOne));
        assertEquals(announcementId.toString(), queryReferenceId(eventId, recipientOne));
        assertEquals("Platform update", queryTitle(eventId, recipientOne));
        assertEquals("New features are live.", queryContent(eventId, recipientOne));
        String metadata = queryMetadata(eventId, recipientOne);
        assertTrue(metadata.contains("\"is_pinned\":true"));
        assertTrue(metadata.contains("\"dismissible\":true"));
        assertTrue(metadata.contains("\"severity\":\"INFO\""));
        assertFalse(metadata.contains("created_by"));
    }

    @Test
    void process_storesNonPinnedMetadataWhenIsPinnedFalse() {
        UUID recipientId = UUID.randomUUID();
        UUID announcementId = UUID.randomUUID();
        UUID eventId = ingestAnnouncementEvent(announcementId, recipientId, null, "INFO", false);

        processNotificationEventUseCase.execute(new ProcessNotificationEventCommand(eventId));

        String metadata = queryMetadata(eventId, recipientId);
        assertTrue(metadata.contains("\"is_pinned\":false"));
    }

    @Test
    void process_marksEventFailedWhenRecipientsMissing() {
        UUID announcementId = UUID.randomUUID();
        UUID eventId = ingestAnnouncementEvent(announcementId, null, null, "INFO", false);

        var result = processNotificationEventUseCase.execute(new ProcessNotificationEventCommand(eventId));

        assertEquals(ProcessNotificationEventOutcome.FAILED, result.outcome());
        assertEquals("FAILED", queryEventStatus(eventId));
    }

    @Test
    void process_isIdempotentForDuplicateFanOut() {
        UUID recipientId = UUID.randomUUID();
        UUID announcementId = UUID.randomUUID();
        UUID eventId = ingestAnnouncementEvent(announcementId, recipientId, null, "WARNING", true);

        processNotificationEventUseCase.execute(new ProcessNotificationEventCommand(eventId));
        notificationEventRepository.save(reopenForReprocess(eventId));
        processNotificationEventUseCase.execute(new ProcessNotificationEventCommand(eventId));

        assertEquals(1, countNotifications(eventId, recipientId));
    }

    @Test
    void process_marksEventFailedWhenAnnouncementIdMissing() {
        UUID eventId = insertAnnouncementEvent(
                "OTHER",
                "not-a-uuid",
                UUID.randomUUID(),
                """
                        {
                          "title":"T",
                          "content":"C",
                          "severity":"INFO",
                          "recipient_user_ids":["%s"]
                        }
                        """.formatted(UUID.randomUUID())
        );

        var result = processNotificationEventUseCase.execute(new ProcessNotificationEventCommand(eventId));

        assertEquals(ProcessNotificationEventOutcome.FAILED, result.outcome());
    }

    private UUID ingestAnnouncementEvent(
            UUID announcementId,
            UUID recipientOne,
            UUID recipientTwo,
            String severity,
            boolean isPinned
    ) {
        String recipientsJson = buildRecipientsJson(recipientOne, recipientTwo);
        var ingestResult = storeNotificationEventUseCase.execute(new NotificationEventIngestCommand(
                UUID.randomUUID(),
                null,
                "SYSTEM_ANNOUNCEMENT_SENT",
                NotificationSourceService.ADMIN,
                "ANNOUNCEMENT",
                announcementId.toString(),
                null,
                null,
                """
                        {
                          "announcement_id":"%s",
                          "title":"Platform update",
                          "content":"New features are live.",
                          "severity":"%s",
                          "is_pinned":%s,
                          "dismissible":true,
                          "created_by":"%s",
                          "status":"SENT"%s
                        }
                        """.formatted(
                        announcementId,
                        severity,
                        isPinned,
                        UUID.randomUUID(),
                        recipientsJson
                )
        ));
        return ingestResult.notificationEventId();
    }

    private String buildRecipientsJson(UUID recipientOne, UUID recipientTwo) {
        if (recipientOne == null && recipientTwo == null) {
            return "";
        }
        if (recipientTwo == null) {
            return ",\n  \"recipient_user_ids\":[\"" + recipientOne + "\"]";
        }
        return ",\n  \"recipient_user_ids\":[\"" + recipientOne + "\",\"" + recipientTwo + "\"]";
    }

    private UUID insertAnnouncementEvent(
            String aggregateType,
            String aggregateId,
            UUID recipientId,
            String payload
    ) {
        UUID eventId = UUID.randomUUID();
        String storedPayload = adminSystemAnnouncementPayloadNormalizer.normalizeForStorage(
                "SYSTEM_ANNOUNCEMENT_SENT",
                payload
        );

        notificationEventRepository.save(new NotificationEvent(
                eventId,
                UUID.randomUUID(),
                null,
                "SYSTEM_ANNOUNCEMENT_SENT",
                NotificationSourceService.ADMIN,
                aggregateType,
                aggregateId,
                null,
                recipientId,
                storedPayload,
                NotificationEventStatus.PENDING,
                0,
                5,
                null,
                null,
                null,
                Instant.now(),
                null
        ));
        return eventId;
    }

    private NotificationEvent reopenForReprocess(UUID eventId) {
        NotificationEvent existing = notificationEventRepository.findById(eventId).orElseThrow();
        return new NotificationEvent(
                existing.id(),
                existing.sourceEventId(),
                existing.eventKey(),
                existing.eventType(),
                existing.sourceService(),
                existing.aggregateType(),
                existing.aggregateId(),
                existing.actorId(),
                existing.recipientUserId(),
                existing.payload(),
                NotificationEventStatus.PENDING,
                existing.retryCount(),
                existing.maxRetryCount(),
                existing.lastError(),
                null,
                null,
                existing.createdAt(),
                null
        );
    }

    private int countNotifications(UUID eventId, UUID userId) {
        Integer count = jdbcTemplate.queryForObject(
                """
                        SELECT COUNT(*) FROM user_notifications
                        WHERE notification_event_id = ? AND user_id = ?
                        """,
                Integer.class,
                eventId,
                userId
        );
        return count == null ? 0 : count;
    }

    private String queryReferenceType(UUID eventId, UUID userId) {
        return jdbcTemplate.queryForObject(
                """
                        SELECT reference_type FROM user_notifications
                        WHERE notification_event_id = ? AND user_id = ?
                        """,
                String.class,
                eventId,
                userId
        );
    }

    private String queryReferenceId(UUID eventId, UUID userId) {
        return jdbcTemplate.queryForObject(
                """
                        SELECT reference_id FROM user_notifications
                        WHERE notification_event_id = ? AND user_id = ?
                        """,
                String.class,
                eventId,
                userId
        );
    }

    private String queryTitle(UUID eventId, UUID userId) {
        return jdbcTemplate.queryForObject(
                """
                        SELECT title FROM user_notifications
                        WHERE notification_event_id = ? AND user_id = ?
                        """,
                String.class,
                eventId,
                userId
        );
    }

    private String queryContent(UUID eventId, UUID userId) {
        return jdbcTemplate.queryForObject(
                """
                        SELECT content FROM user_notifications
                        WHERE notification_event_id = ? AND user_id = ?
                        """,
                String.class,
                eventId,
                userId
        );
    }

    private String queryMetadata(UUID eventId, UUID userId) {
        return NotificationEventPayloadCodec.decode(jdbcTemplate.queryForObject(
                """
                        SELECT metadata FROM user_notifications
                        WHERE notification_event_id = ? AND user_id = ?
                        """,
                String.class,
                eventId,
                userId
        ));
    }

    private String queryEventStatus(UUID eventId) {
        return jdbcTemplate.queryForObject(
                "SELECT status FROM notification_events WHERE id = ?",
                String.class,
                eventId
        );
    }
}
