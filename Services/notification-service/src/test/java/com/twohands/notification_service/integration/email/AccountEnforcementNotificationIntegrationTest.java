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
class AccountEnforcementNotificationIntegrationTest {

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
    void process_sendsSuspensionEmailFromAdminStylePayload() {
        UUID userId = UUID.randomUUID();
        UUID eventId = ingestAdminEnforcementEvent(
                userId,
                "USER_SUSPENDED",
                "user@example.com",
                "Repeated policy violations",
                "SPAM_ABUSE",
                "2026-12-31T00:00:00Z"
        );

        var result = processNotificationEventUseCase.execute(new ProcessNotificationEventCommand(eventId));

        assertEquals(ProcessNotificationEventOutcome.COMPLETED, result.outcome());

        NotificationEvent stored = notificationEventRepository.findById(eventId).orElseThrow();
        assertTrue(stored.payload().contains("enforcement_reason"));
        assertFalse(stored.payload().contains("enforced_by"));
        assertFalse(stored.payload().contains("internal"));
    }

    @Test
    void process_sendsRestrictedEmailEvenWhenUserDisabledEmail() {
        UUID userId = UUID.randomUUID();
        jdbcTemplate.update(
                """
                        INSERT INTO user_notification_settings(
                            user_id, event_type, allow_push, allow_email, allow_in_app, created_at, updated_at
                        )
                        VALUES (?, 'USER_RESTRICTED', TRUE, FALSE, FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                        """,
                userId
        );

        UUID eventId = insertEnforcementEvent(
                userId,
                "USER_RESTRICTED",
                """
                        {
                          "target_user_id":"%s",
                          "recipient_email":"user@example.com",
                          "enforcement_reason":"Policy violation"
                        }
                        """.formatted(userId)
        );

        var result = processNotificationEventUseCase.execute(new ProcessNotificationEventCommand(eventId));

        assertEquals(ProcessNotificationEventOutcome.COMPLETED, result.outcome());
    }

    @Test
    void process_marksEventFailedWhenRecipientEmailMissing() {
        UUID userId = UUID.randomUUID();
        UUID eventId = insertEnforcementEvent(
                userId,
                "USER_SUSPENDED",
                """
                        {
                          "target_user_id":"%s",
                          "enforcement_reason":"Spam abuse"
                        }
                        """.formatted(userId)
        );

        var result = processNotificationEventUseCase.execute(new ProcessNotificationEventCommand(eventId));

        assertEquals(ProcessNotificationEventOutcome.FAILED, result.outcome());
        String lastError = jdbcTemplate.queryForObject(
                "SELECT last_error FROM notification_events WHERE id = ?",
                String.class,
                eventId
        );
        assertTrue(lastError.contains("recipient") || lastError.contains("email"));
    }

    private UUID ingestAdminEnforcementEvent(
            UUID userId,
            String eventType,
            String email,
            String description,
            String reasonCode,
            String expiresAt
    ) {
        UUID sourceEventId = UUID.randomUUID();
        var ingestResult = storeNotificationEventUseCase.execute(new NotificationEventIngestCommand(
                sourceEventId,
                null,
                eventType,
                NotificationSourceService.ADMIN,
                "USER",
                userId.toString(),
                null,
                userId,
                """
                        {
                          "user_id":"%s",
                          "email":"%s",
                          "enforcement_id":"%s",
                          "reason_code":"%s",
                          "description":"%s",
                          "expires_at":"%s",
                          "enforced_by":"%s",
                          "note":"internal admin note"
                        }
                        """.formatted(
                        userId,
                        email,
                        UUID.randomUUID(),
                        reasonCode,
                        description,
                        expiresAt,
                        UUID.randomUUID()
                )
        ));
        return ingestResult.notificationEventId();
    }

    private UUID insertEnforcementEvent(UUID userId, String eventType, String payload) {
        UUID eventId = UUID.randomUUID();
        notificationEventRepository.save(new NotificationEvent(
                eventId,
                UUID.randomUUID(),
                null,
                eventType,
                NotificationSourceService.ADMIN,
                "USER",
                userId.toString(),
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
}
