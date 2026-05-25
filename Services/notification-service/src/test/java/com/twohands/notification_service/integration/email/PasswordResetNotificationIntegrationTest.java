package com.twohands.notification_service.integration.email;

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
@TestPropertySource(properties = {
        "notification.integrations.email.enabled=true",
        "notification.integrations.email.password-reset-link-base-url=https://2hands.vn/reset-password"
})
class PasswordResetNotificationIntegrationTest {

    @Autowired
    private ProcessNotificationEventUseCase processNotificationEventUseCase;

    @Autowired
    private StoreNotificationEventUseCase storeNotificationEventUseCase;

    @Autowired
    private NotificationEventRepository notificationEventRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void cleanTables() {
        jdbcTemplate.execute("DELETE FROM user_notification_settings");
        jdbcTemplate.execute("DELETE FROM notification_events");
    }

    @Test
    void process_sendsPasswordResetEmailFromAuthStylePayload() {
        UUID userId = UUID.randomUUID();
        UUID eventId = ingestAuthPasswordResetEvent(userId, "user@example.com", "reset-token-xyz");

        var result = processNotificationEventUseCase.execute(new ProcessNotificationEventCommand(eventId));

        assertEquals(ProcessNotificationEventOutcome.COMPLETED, result.outcome());
        assertEquals("COMPLETED", queryEventStatus(eventId));

        NotificationEvent stored = notificationEventRepository.findById(eventId).orElseThrow();
        assertTrue(stored.payload().contains("recipient_email"));
        assertTrue(stored.payload().contains("reset_link"));
        assertFalse(stored.payload().contains("\"verification_token\""));
    }

    @Test
    void process_marksEventFailedWhenResetDeliveryMissing() {
        UUID userId = UUID.randomUUID();
        UUID eventId = insertPasswordResetEvent(
                userId,
                """
                        {"user_id":"%s","recipient_email":"user@example.com"}
                        """.formatted(userId)
        );

        var result = processNotificationEventUseCase.execute(new ProcessNotificationEventCommand(eventId));

        assertEquals(ProcessNotificationEventOutcome.FAILED, result.outcome());
        assertEquals("FAILED", queryEventStatus(eventId));
        String lastError = jdbcTemplate.queryForObject(
                "SELECT last_error FROM notification_events WHERE id = ?",
                String.class,
                eventId
        );
        assertTrue(lastError.contains("reset_link") || lastError.contains("reset"));
        assertFalse(lastError.contains("reset-token-secret"));
    }

    @Test
    void process_sendsEvenWhenUserDisabledEmailForEventType() {
        UUID userId = UUID.randomUUID();
        jdbcTemplate.update(
                """
                        INSERT INTO user_notification_settings(
                            user_id, event_type, allow_push, allow_email, allow_in_app, created_at, updated_at
                        )
                        VALUES (?, 'PASSWORD_RESET_REQUESTED', TRUE, FALSE, FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                        """,
                userId
        );

        UUID eventId = insertPasswordResetEvent(
                userId,
                """
                        {
                          "user_id":"%s",
                          "recipient_email":"user@example.com",
                          "reset_code":"987654"
                        }
                        """.formatted(userId)
        );

        var result = processNotificationEventUseCase.execute(new ProcessNotificationEventCommand(eventId));

        assertEquals(ProcessNotificationEventOutcome.COMPLETED, result.outcome());
    }

    private UUID ingestAuthPasswordResetEvent(UUID userId, String email, String resetToken) {
        UUID sourceEventId = UUID.randomUUID();
        var ingestResult = storeNotificationEventUseCase.execute(new NotificationEventIngestCommand(
                sourceEventId,
                null,
                "PASSWORD_RESET_REQUESTED",
                NotificationSourceService.AUTH,
                null,
                null,
                null,
                userId,
                """
                        {
                          "user_id":"%s",
                          "email":"%s",
                          "verification_token":"%s",
                          "verification_token_type":"PASSWORD_RESET"
                        }
                        """.formatted(userId, email, resetToken)
        ));
        return ingestResult.notificationEventId();
    }

    private UUID insertPasswordResetEvent(UUID userId, String payload) {
        UUID eventId = UUID.randomUUID();
        notificationEventRepository.save(new NotificationEvent(
                eventId,
                UUID.randomUUID(),
                null,
                "PASSWORD_RESET_REQUESTED",
                NotificationSourceService.AUTH,
                null,
                null,
                null,
                userId,
                payload,
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

    private String queryEventStatus(UUID eventId) {
        return jdbcTemplate.queryForObject(
                "SELECT status FROM notification_events WHERE id = ?",
                String.class,
                eventId
        );
    }
}
