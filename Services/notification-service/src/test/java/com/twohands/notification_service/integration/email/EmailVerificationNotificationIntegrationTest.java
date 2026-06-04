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
@TestPropertySource(properties = "notification.integrations.email.enabled=true")
class EmailVerificationNotificationIntegrationTest {

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
    void process_sendsVerificationEmailFromAuthStylePayload() {
        UUID userId = UUID.randomUUID();
        UUID eventId = ingestAuthVerificationEvent(userId, "user@example.com", "123456");

        var result = processNotificationEventUseCase.execute(new ProcessNotificationEventCommand(eventId));

        assertEquals(ProcessNotificationEventOutcome.COMPLETED, result.outcome());
        assertEquals("COMPLETED", queryEventStatus(eventId));

        NotificationEvent stored = notificationEventRepository.findById(eventId).orElseThrow();
        assertTrue(stored.payload().contains("recipient_email"));
        assertTrue(stored.payload().contains("verification_code"));
        assertFalse(stored.payload().contains("verification_link"));
        assertFalse(stored.payload().contains("\"verification_token\""));
    }

    @Test
    void process_marksEventFailedWhenVerificationDeliveryMissing() {
        UUID userId = UUID.randomUUID();
        UUID eventId = insertVerificationEvent(
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
        assertTrue(
                lastError != null
                        && (lastError.contains("verification_code")
                        || lastError.toLowerCase().contains("verification"))
        );
    }

    @Test
    void process_sendsEvenWhenUserDisabledEmailForEventType() {
        UUID userId = UUID.randomUUID();
        jdbcTemplate.update(
                """
                        INSERT INTO user_notification_settings(
                            user_id, event_type, allow_push, allow_email, allow_in_app, created_at, updated_at
                        )
                        VALUES (?, 'EMAIL_VERIFICATION_REQUESTED', TRUE, FALSE, FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                        """,
                userId
        );

        UUID eventId = insertVerificationEvent(
                userId,
                """
                        {
                          "user_id":"%s",
                          "recipient_email":"user@example.com",
                          "verification_code":"123456"
                        }
                        """.formatted(userId)
        );

        var result = processNotificationEventUseCase.execute(new ProcessNotificationEventCommand(eventId));

        assertEquals(ProcessNotificationEventOutcome.COMPLETED, result.outcome());
    }

    private UUID ingestAuthVerificationEvent(UUID userId, String email, String verificationToken) {
        UUID sourceEventId = UUID.randomUUID();
        var ingestResult = storeNotificationEventUseCase.execute(new NotificationEventIngestCommand(
                sourceEventId,
                null,
                "EMAIL_VERIFICATION_REQUESTED",
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
                          "verification_token_type":"EMAIL_VERIFY"
                        }
                        """.formatted(userId, email, verificationToken)
        ));
        return ingestResult.notificationEventId();
    }

    private UUID insertVerificationEvent(UUID userId, String payload) {
        UUID eventId = UUID.randomUUID();
        notificationEventRepository.save(new NotificationEvent(
                eventId,
                UUID.randomUUID(),
                null,
                "EMAIL_VERIFICATION_REQUESTED",
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
