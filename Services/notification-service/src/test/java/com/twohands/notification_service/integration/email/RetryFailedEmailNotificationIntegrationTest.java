package com.twohands.notification_service.integration.email;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.twohands.notification_service.application.delivery.EmailDeliveryRetryMetadataCodec;
import com.twohands.notification_service.application.delivery.RecordEmailDeliveryFailureCommand;
import com.twohands.notification_service.application.delivery.RecordEmailDeliveryFailureUseCase;
import com.twohands.notification_service.application.email.RetryFailedEmailNotificationUseCase;
import com.twohands.notification_service.application.worker.NotificationFailurePolicy;
import com.twohands.notification_service.domain.delivery.EmailDeliveryRetryPolicy;
import com.twohands.notification_service.domain.delivery.EmailDeliveryRetryState;
import com.twohands.notification_service.domain.usernotification.NotificationDeliveryStatus;
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
@TestPropertySource(properties = {
        "notification.integrations.email.enabled=true",
        "notification.workers.retry-delivery.base-backoff-seconds=0"
})
class RetryFailedEmailNotificationIntegrationTest {

    @Autowired
    private RetryFailedEmailNotificationUseCase retryFailedEmailNotificationUseCase;

    @Autowired
    private RecordEmailDeliveryFailureUseCase recordEmailDeliveryFailureUseCase;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private EmailDeliveryRetryMetadataCodec metadataCodec;

    @BeforeEach
    void setUp() {
        metadataCodec = new EmailDeliveryRetryMetadataCodec(new ObjectMapper());
        jdbcTemplate.execute("DELETE FROM user_notifications");
        jdbcTemplate.execute("DELETE FROM notification_events");
        jdbcTemplate.execute("DELETE FROM user_notification_settings");
    }

    @Test
    void execute_retriesAndMarksSentWhenEmailSucceeds() {
        UUID userId = UUID.randomUUID();
        UUID eventId = UUID.randomUUID();
        UUID notificationId = UUID.randomUUID();
        insertNotificationEvent(eventId, userId);
        insertFailedNotification(notificationId, eventId, userId, retryableMetadata(1, Instant.parse("2020-01-01T00:00:00Z")));

        int processed = retryFailedEmailNotificationUseCase.execute(10);

        assertEquals(1, processed);
        assertEquals("SENT", queryDeliveryStatus(notificationId));
        assertFalse(queryMetadata(notificationId).contains("emailDelivery"));
    }

    @Test
    void execute_incrementsRetryCountOnRetryableProviderFailure() {
        UUID userId = UUID.randomUUID();
        UUID eventId = UUID.randomUUID();
        UUID notificationId = UUID.randomUUID();
        insertNotificationEvent(eventId, userId, "retryable-email@example.com");
        insertFailedNotification(notificationId, eventId, userId, retryableMetadata(1, Instant.parse("2020-01-01T00:00:00Z")));

        retryFailedEmailNotificationUseCase.execute(10);

        assertEquals("FAILED", queryDeliveryStatus(notificationId));
        var state = metadataCodec.parse(queryMetadata(notificationId));
        assertTrue(state.isPresent());
        assertEquals(2, state.get().retryCount());
        assertEquals(NotificationFailurePolicy.RETRYABLE, state.get().failurePolicy());
    }

    @Test
    void execute_skipsPermanentFailures() {
        UUID userId = UUID.randomUUID();
        UUID eventId = UUID.randomUUID();
        UUID notificationId = UUID.randomUUID();
        insertNotificationEvent(eventId, userId);
        String metadata = metadataCodec.mergeEmailDeliveryState(
                "{}",
                new EmailDeliveryRetryState(
                        NotificationFailurePolicy.PERMANENT,
                        5,
                        5,
                        "Invalid email",
                        Instant.parse("2020-01-01T00:00:00Z")
                )
        );
        insertFailedNotification(notificationId, eventId, userId, metadata);

        int processed = retryFailedEmailNotificationUseCase.execute(10);

        assertEquals(0, processed);
        assertEquals("FAILED", queryDeliveryStatus(notificationId));
    }

    @Test
    void recordEmailDeliveryFailure_persistsRetryableMetadata() {
        UUID userId = UUID.randomUUID();
        UUID eventId = UUID.randomUUID();
        UUID notificationId = UUID.randomUUID();
        insertNotificationEvent(eventId, userId);
        insertSentNotification(notificationId, eventId, userId, "ORDER_CREATED");

        var updated = recordEmailDeliveryFailureUseCase.execute(new RecordEmailDeliveryFailureCommand(
                notificationId,
                NotificationFailurePolicy.RETRYABLE,
                "Email provider timeout."
        ));

        assertEquals(NotificationDeliveryStatus.FAILED, updated.deliveryStatus());
        var state = metadataCodec.parse(updated.metadata());
        assertTrue(state.isPresent());
        assertEquals(NotificationFailurePolicy.RETRYABLE, state.get().failurePolicy());
        assertEquals(1, state.get().retryCount());
    }

    private String retryableMetadata(int retryCount, Instant lastAttemptAt) {
        return metadataCodec.mergeEmailDeliveryState(
                "{}",
                new EmailDeliveryRetryState(
                        NotificationFailurePolicy.RETRYABLE,
                        retryCount,
                        EmailDeliveryRetryPolicy.DEFAULT_MAX_RETRY_COUNT,
                        "Email provider timeout.",
                        lastAttemptAt
                )
        );
    }

    private void insertNotificationEvent(UUID eventId, UUID recipientId) {
        insertNotificationEvent(eventId, recipientId, "buyer@example.com");
    }

    private void insertNotificationEvent(UUID eventId, UUID recipientId, String email) {
        jdbcTemplate.update(
                """
                        INSERT INTO notification_events(
                            id, source_event_id, event_type, source_service, aggregate_type, aggregate_id,
                            recipient_user_id, payload, status, retry_count, max_retry_count, created_at
                        )
                        VALUES (?, ?, 'ORDER_CREATED', 'COMMERCE', 'ORDER', 'ord-1', ?, ?, 'COMPLETED', 0, 5, CURRENT_TIMESTAMP)
                        """,
                eventId,
                UUID.randomUUID(),
                recipientId,
                """
                        {"recipient_email":"%s","order_code":"ORD-1"}
                        """.formatted(email)
        );
    }

    private void insertFailedNotification(UUID notificationId, UUID eventId, UUID userId, String metadata) {
        insertNotification(notificationId, eventId, userId, "ORDER_CREATED", metadata, "FAILED");
    }

    private void insertSentNotification(UUID notificationId, UUID eventId, UUID userId, String eventType) {
        insertNotification(notificationId, eventId, userId, eventType, "{}", "SENT");
    }

    private void insertNotification(
            UUID notificationId,
            UUID eventId,
            UUID userId,
            String eventType,
            String metadata,
            String deliveryStatus
    ) {
        jdbcTemplate.update(
                """
                        INSERT INTO user_notifications(
                            id, notification_event_id, user_id, type, title, content,
                            reference_type, reference_id, is_read, is_deleted, metadata,
                            delivery_status, created_at
                        )
                        VALUES (?, ?, ?, ?, 'Title', 'Content', 'ORDER', 'ord-1', FALSE, FALSE, ?, ?, CURRENT_TIMESTAMP)
                        """,
                notificationId,
                eventId,
                userId,
                eventType,
                metadata,
                deliveryStatus
        );
    }

    private String queryDeliveryStatus(UUID notificationId) {
        return jdbcTemplate.queryForObject(
                "SELECT delivery_status FROM user_notifications WHERE id = ?",
                String.class,
                notificationId
        );
    }

    private String queryMetadata(UUID notificationId) {
        return jdbcTemplate.queryForObject(
                "SELECT metadata FROM user_notifications WHERE id = ?",
                String.class,
                notificationId
        );
    }
}
