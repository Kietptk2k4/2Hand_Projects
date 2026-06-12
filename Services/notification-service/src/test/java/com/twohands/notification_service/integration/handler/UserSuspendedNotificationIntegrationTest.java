package com.twohands.notification_service.integration.handler;

import com.twohands.notification_service.application.email.AccountEnforcementEmailPayloadNormalizer;
import com.twohands.notification_service.application.ingest.NotificationEventIngestCommand;
import com.twohands.notification_service.application.ingest.StoreNotificationEventUseCase;
import com.twohands.notification_service.application.worker.ProcessNotificationEventCommand;
import com.twohands.notification_service.application.worker.ProcessNotificationEventOutcome;
import com.twohands.notification_service.application.worker.ProcessNotificationEventUseCase;
import com.twohands.notification_service.domain.notificationevent.NotificationEvent;
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
class UserSuspendedNotificationIntegrationTest {

    @Autowired
    private ProcessNotificationEventUseCase processNotificationEventUseCase;

    @Autowired
    private StoreNotificationEventUseCase storeNotificationEventUseCase;

    @Autowired
    private NotificationEventRepository notificationEventRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private AccountEnforcementEmailPayloadNormalizer accountEnforcementEmailPayloadNormalizer;

    @BeforeEach
    void cleanTables() {
        jdbcTemplate.execute("DELETE FROM user_notification_settings");
        jdbcTemplate.execute("DELETE FROM user_notifications");
        jdbcTemplate.execute("DELETE FROM notification_events");
    }

    @Test
    void process_notifiesTargetUserForBannedEvent() {
        UUID userId = UUID.randomUUID();
        UUID enforcementId = UUID.randomUUID();
        UUID eventId = ingestBannedEvent(userId, enforcementId, "Confirmed payment fraud");

        var result = processNotificationEventUseCase.execute(new ProcessNotificationEventCommand(eventId));

        assertEquals(ProcessNotificationEventOutcome.COMPLETED, result.outcome());
        assertEquals(1, countNotifications(eventId, userId));
        assertEquals("USER_ENFORCEMENT", queryReferenceType(eventId, userId));
        assertEquals(enforcementId.toString(), queryReferenceId(eventId, userId));
        assertEquals("Account banned", queryTitle(eventId, userId));
        assertTrue(queryMetadata(eventId, userId).contains("enforcement_reason"));
        assertFalse(queryMetadata(eventId, userId).contains("enforced_by"));
    }

    @Test
    void process_notifiesTargetUserWithEnforcementReference() {
        UUID userId = UUID.randomUUID();
        UUID enforcementId = UUID.randomUUID();
        UUID eventId = ingestSuspendedEvent(userId, enforcementId, "Repeated policy violations");

        var result = processNotificationEventUseCase.execute(new ProcessNotificationEventCommand(eventId));

        assertEquals(ProcessNotificationEventOutcome.COMPLETED, result.outcome());
        assertEquals(1, countNotifications(eventId, userId));
        assertEquals("USER_ENFORCEMENT", queryReferenceType(eventId, userId));
        assertEquals(enforcementId.toString(), queryReferenceId(eventId, userId));
        assertEquals("Account suspended", queryTitle(eventId, userId));
        assertTrue(queryMetadata(eventId, userId).contains("enforcement_reason"));
        assertFalse(queryMetadata(eventId, userId).contains("enforced_by"));
        assertFalse(queryMetadata(eventId, userId).contains("internal"));
    }

    @Test
    void process_marksEventFailedWhenTargetUserMissing() {
        UUID eventId = insertSuspendedEvent(null, UUID.randomUUID(), "{}");

        var result = processNotificationEventUseCase.execute(new ProcessNotificationEventCommand(eventId));

        assertEquals(ProcessNotificationEventOutcome.FAILED, result.outcome());
        assertEquals("FAILED", queryEventStatus(eventId));
    }

    @Test
    void process_notifiesWhenUserDisabledPushAndEmailButInAppEnabled() {
        UUID userId = UUID.randomUUID();
        UUID enforcementId = UUID.randomUUID();
        jdbcTemplate.update(
                """
                        INSERT INTO user_notification_settings(
                            user_id, event_type, allow_push, allow_email, allow_in_app, created_at, updated_at
                        )
                        VALUES (?, 'USER_SUSPENDED', FALSE, FALSE, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                        """,
                userId
        );

        UUID eventId = ingestSuspendedEvent(userId, enforcementId, "Spam abuse");

        var result = processNotificationEventUseCase.execute(new ProcessNotificationEventCommand(eventId));

        assertEquals(ProcessNotificationEventOutcome.COMPLETED, result.outcome());
        assertEquals(1, countNotifications(eventId, userId));
    }

    @Test
    void process_isIdempotentForDuplicateDelivery() {
        UUID userId = UUID.randomUUID();
        UUID enforcementId = UUID.randomUUID();
        UUID eventId = ingestSuspendedEvent(userId, enforcementId, "Policy violation");

        processNotificationEventUseCase.execute(new ProcessNotificationEventCommand(eventId));
        notificationEventRepository.save(reopenForReprocess(eventId));
        processNotificationEventUseCase.execute(new ProcessNotificationEventCommand(eventId));

        assertEquals(1, countNotifications(eventId, userId));
    }

    private UUID ingestBannedEvent(UUID userId, UUID enforcementId, String description) {
        var ingestResult = storeNotificationEventUseCase.execute(new NotificationEventIngestCommand(
                UUID.randomUUID(),
                null,
                "USER_BANNED",
                NotificationSourceService.ADMIN,
                "USER_ENFORCEMENT",
                enforcementId.toString(),
                null,
                userId,
                """
                        {
                          "user_id":"%s",
                          "enforcement_id":"%s",
                          "action_type":"BAN",
                          "reason_code":"FRAUD",
                          "description":"%s",
                          "expires_at":null,
                          "enforced_by":"%s"
                        }
                        """.formatted(userId, enforcementId, description, UUID.randomUUID())
        ));
        return ingestResult.notificationEventId();
    }

    private UUID ingestSuspendedEvent(UUID userId, UUID enforcementId, String description) {
        var ingestResult = storeNotificationEventUseCase.execute(new NotificationEventIngestCommand(
                UUID.randomUUID(),
                null,
                "USER_SUSPENDED",
                NotificationSourceService.ADMIN,
                "USER_ENFORCEMENT",
                enforcementId.toString(),
                null,
                userId,
                """
                        {
                          "user_id":"%s",
                          "enforcement_id":"%s",
                          "reason_code":"SPAM_ABUSE",
                          "description":"%s",
                          "expires_at":"2026-12-31T00:00:00Z",
                          "enforced_by":"%s",
                          "note":"internal admin note"
                        }
                        """.formatted(userId, enforcementId, description, UUID.randomUUID())
        ));
        return ingestResult.notificationEventId();
    }

    private UUID insertSuspendedEvent(UUID userId, UUID enforcementId, String payload) {
        UUID eventId = UUID.randomUUID();
        String storedPayload = accountEnforcementEmailPayloadNormalizer.normalizeForStorage(
                "USER_SUSPENDED",
                payload
        );

        notificationEventRepository.save(new NotificationEvent(
                eventId,
                UUID.randomUUID(),
                null,
                "USER_SUSPENDED",
                NotificationSourceService.ADMIN,
                "USER_ENFORCEMENT",
                enforcementId.toString(),
                null,
                userId,
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

    private String queryMetadata(UUID eventId, UUID userId) {
        return jdbcTemplate.queryForObject(
                """
                        SELECT metadata FROM user_notifications
                        WHERE notification_event_id = ? AND user_id = ?
                        """,
                String.class,
                eventId,
                userId
        );
    }

    private String queryEventStatus(UUID eventId) {
        return jdbcTemplate.queryForObject(
                "SELECT status FROM notification_events WHERE id = ?",
                String.class,
                eventId
        );
    }
}
