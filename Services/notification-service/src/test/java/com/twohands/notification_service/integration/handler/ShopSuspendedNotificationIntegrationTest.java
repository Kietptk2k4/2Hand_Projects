package com.twohands.notification_service.integration.handler;

import com.twohands.notification_service.application.email.AdminShopModerationPayloadNormalizer;
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
class ShopSuspendedNotificationIntegrationTest {

    @Autowired
    private ProcessNotificationEventUseCase processNotificationEventUseCase;

    @Autowired
    private StoreNotificationEventUseCase storeNotificationEventUseCase;

    @Autowired
    private NotificationEventRepository notificationEventRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private AdminShopModerationPayloadNormalizer adminShopModerationPayloadNormalizer;

    @BeforeEach
    void cleanTables() {
        jdbcTemplate.execute("DELETE FROM user_notification_settings");
        jdbcTemplate.execute("DELETE FROM user_notifications");
        jdbcTemplate.execute("DELETE FROM notification_events");
    }

    @Test
    void process_notifiesShopOwnerWithShopReference() {
        UUID shopOwnerId = UUID.randomUUID();
        UUID shopId = UUID.randomUUID();
        UUID eventId = ingestShopSuspendedEvent(shopOwnerId, shopId);

        var result = processNotificationEventUseCase.execute(new ProcessNotificationEventCommand(eventId));

        assertEquals(ProcessNotificationEventOutcome.COMPLETED, result.outcome());
        assertEquals(1, countNotifications(eventId, shopOwnerId));
        assertEquals("SHOP", queryReferenceType(eventId, shopOwnerId));
        assertEquals(shopId.toString(), queryReferenceId(eventId, shopOwnerId));
        assertEquals("Cửa hàng bị đình chỉ", queryTitle(eventId, shopOwnerId));
        assertTrue(queryMetadata(eventId, shopOwnerId).contains("suspension_reason"));
        assertFalse(queryMetadata(eventId, shopOwnerId).contains("suspended_by"));
    }

    @Test
    void process_marksEventFailedWhenShopOwnerMissing() {
        UUID eventId = insertShopSuspendedEvent(null, UUID.randomUUID(), "{}");

        var result = processNotificationEventUseCase.execute(new ProcessNotificationEventCommand(eventId));

        assertEquals(ProcessNotificationEventOutcome.FAILED, result.outcome());
        assertEquals("FAILED", queryEventStatus(eventId));
    }

    @Test
    void process_isIdempotentForDuplicateDelivery() {
        UUID shopOwnerId = UUID.randomUUID();
        UUID shopId = UUID.randomUUID();
        UUID eventId = ingestShopSuspendedEvent(shopOwnerId, shopId);

        processNotificationEventUseCase.execute(new ProcessNotificationEventCommand(eventId));
        notificationEventRepository.save(reopenForReprocess(eventId));
        processNotificationEventUseCase.execute(new ProcessNotificationEventCommand(eventId));

        assertEquals(1, countNotifications(eventId, shopOwnerId));
    }

    private UUID ingestShopSuspendedEvent(UUID shopOwnerId, UUID shopId) {
        var ingestResult = storeNotificationEventUseCase.execute(new NotificationEventIngestCommand(
                UUID.randomUUID(),
                null,
                "SHOP_SUSPENDED",
                NotificationSourceService.ADMIN,
                "SHOP",
                shopId.toString(),
                null,
                shopOwnerId,
                """
                        {
                          "shop_id":"%s",
                          "shop_owner_id":"%s",
                          "reason":"Policy violation",
                          "expires_at":"2026-12-31T00:00:00Z",
                          "suspended_by":"%s",
                          "note":"internal admin note"
                        }
                        """.formatted(shopId, shopOwnerId, UUID.randomUUID())
        ));
        return ingestResult.notificationEventId();
    }

    private UUID insertShopSuspendedEvent(UUID shopOwnerId, UUID shopId, String payload) {
        UUID eventId = UUID.randomUUID();
        String storedPayload = adminShopModerationPayloadNormalizer.normalizeForStorage(
                "SHOP_SUSPENDED",
                payload
        );

        notificationEventRepository.save(new NotificationEvent(
                eventId,
                UUID.randomUUID(),
                null,
                "SHOP_SUSPENDED",
                NotificationSourceService.ADMIN,
                "SHOP",
                shopId.toString(),
                null,
                shopOwnerId,
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
