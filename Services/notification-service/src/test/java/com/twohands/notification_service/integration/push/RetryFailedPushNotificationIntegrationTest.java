package com.twohands.notification_service.integration.push;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.twohands.notification_service.application.delivery.PushDeliveryRetryMetadataCodec;
import com.twohands.notification_service.application.delivery.RecordPushDeliveryFailureCommand;
import com.twohands.notification_service.application.delivery.RecordPushDeliveryFailureUseCase;
import com.twohands.notification_service.application.push.RetryFailedPushNotificationUseCase;
import com.twohands.notification_service.application.worker.NotificationFailurePolicy;
import com.twohands.notification_service.domain.delivery.PushDeliveryRetryPolicy;
import com.twohands.notification_service.domain.delivery.PushDeliveryRetryState;
import com.twohands.notification_service.domain.notificationevent.NotificationEventPayloadCodec;
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
        "notification.integrations.fcm.enabled=true",
        "notification.workers.retry-delivery.base-backoff-seconds=0"
})
class RetryFailedPushNotificationIntegrationTest {

    @Autowired
    private RetryFailedPushNotificationUseCase retryFailedPushNotificationUseCase;

    @Autowired
    private RecordPushDeliveryFailureUseCase recordPushDeliveryFailureUseCase;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private PushDeliveryRetryMetadataCodec metadataCodec;

    @BeforeEach
    void setUp() {
        metadataCodec = new PushDeliveryRetryMetadataCodec(new ObjectMapper());
        jdbcTemplate.execute("DELETE FROM user_device_tokens");
        jdbcTemplate.execute("DELETE FROM user_notifications");
        jdbcTemplate.execute("DELETE FROM user_notification_settings");
    }

    @Test
    void execute_retriesAndMarksSentWhenPushSucceeds() {
        UUID userId = UUID.randomUUID();
        UUID notificationId = UUID.randomUUID();
        insertActiveToken(userId, "fcm-valid-token");
        insertFailedNotification(notificationId, userId, retryableMetadata(1, Instant.parse("2020-01-01T00:00:00Z")));

        int processed = retryFailedPushNotificationUseCase.execute(10);

        assertEquals(1, processed);
        assertEquals("SENT", queryDeliveryStatus(notificationId));
        assertFalse(queryMetadata(notificationId).contains("pushDelivery"));
    }

    @Test
    void execute_incrementsRetryCountOnRetryableProviderFailure() {
        UUID userId = UUID.randomUUID();
        UUID notificationId = UUID.randomUUID();
        insertActiveToken(userId, "device-retryable-token-xyz");
        insertFailedNotification(notificationId, userId, retryableMetadata(1, Instant.parse("2020-01-01T00:00:00Z")));

        retryFailedPushNotificationUseCase.execute(10);

        assertEquals("FAILED", queryDeliveryStatus(notificationId));
        var state = metadataCodec.parse(queryMetadata(notificationId));
        assertTrue(state.isPresent());
        assertEquals(2, state.get().retryCount());
        assertEquals(NotificationFailurePolicy.RETRYABLE, state.get().failurePolicy());
    }

    @Test
    void execute_skipsPermanentFailures() {
        UUID userId = UUID.randomUUID();
        UUID notificationId = UUID.randomUUID();
        insertActiveToken(userId, "fcm-valid-token");
        String metadata = metadataCodec.mergePushDeliveryState(
                "{}",
                new PushDeliveryRetryState(
                        NotificationFailurePolicy.PERMANENT,
                        5,
                        5,
                        "Invalid token",
                        Instant.parse("2020-01-01T00:00:00Z")
                )
        );
        insertFailedNotification(notificationId, userId, metadata);

        int processed = retryFailedPushNotificationUseCase.execute(10);

        assertEquals(0, processed);
        assertEquals("FAILED", queryDeliveryStatus(notificationId));
    }

    @Test
    void recordPushDeliveryFailure_persistsRetryableMetadata() {
        UUID userId = UUID.randomUUID();
        UUID notificationId = UUID.randomUUID();
        insertSentNotification(notificationId, userId);

        var updated = recordPushDeliveryFailureUseCase.execute(new RecordPushDeliveryFailureCommand(
                notificationId,
                NotificationFailurePolicy.RETRYABLE,
                "FCM provider timeout."
        ));

        assertEquals(NotificationDeliveryStatus.FAILED, updated.deliveryStatus());
        var state = metadataCodec.parse(updated.metadata());
        assertTrue(state.isPresent());
        assertEquals(NotificationFailurePolicy.RETRYABLE, state.get().failurePolicy());
        assertEquals(1, state.get().retryCount());
    }

    private String retryableMetadata(int retryCount, Instant lastAttemptAt) {
        return metadataCodec.mergePushDeliveryState(
                "{}",
                new PushDeliveryRetryState(
                        NotificationFailurePolicy.RETRYABLE,
                        retryCount,
                        PushDeliveryRetryPolicy.DEFAULT_MAX_RETRY_COUNT,
                        "FCM provider timeout.",
                        lastAttemptAt
                )
        );
    }

    private void insertFailedNotification(UUID notificationId, UUID userId, String metadata) {
        jdbcTemplate.update(
                """
                        INSERT INTO user_notifications(
                            id, notification_event_id, user_id, type, title, content,
                            reference_type, reference_id, is_read, is_deleted, metadata,
                            delivery_status, created_at
                        )
                        VALUES (?, ?, ?, 'POST_LIKED', 'New like', 'Someone liked your post.',
                                'POST', 'post-1', FALSE, FALSE, ?, 'FAILED', CURRENT_TIMESTAMP)
                        """,
                notificationId,
                UUID.randomUUID(),
                userId,
                metadata
        );
    }

    private void insertSentNotification(UUID notificationId, UUID userId) {
        jdbcTemplate.update(
                """
                        INSERT INTO user_notifications(
                            id, notification_event_id, user_id, type, title, content,
                            reference_type, reference_id, is_read, is_deleted, metadata,
                            delivery_status, created_at
                        )
                        VALUES (?, ?, ?, 'POST_LIKED', 'New like', 'Someone liked your post.',
                                'POST', 'post-1', FALSE, FALSE, '{}', 'SENT', CURRENT_TIMESTAMP)
                        """,
                notificationId,
                UUID.randomUUID(),
                userId
        );
    }

    private void insertActiveToken(UUID userId, String deviceToken) {
        jdbcTemplate.update(
                """
                        INSERT INTO user_device_tokens(
                            id, user_id, device_type, device_token, is_active, updated_at, created_at
                        )
                        VALUES (?, ?, 'ANDROID', ?, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                        """,
                UUID.randomUUID(),
                userId,
                deviceToken
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
        return NotificationEventPayloadCodec.decode(jdbcTemplate.queryForObject(
                "SELECT metadata FROM user_notifications WHERE id = ?",
                String.class,
                notificationId
        ));
    }
}
