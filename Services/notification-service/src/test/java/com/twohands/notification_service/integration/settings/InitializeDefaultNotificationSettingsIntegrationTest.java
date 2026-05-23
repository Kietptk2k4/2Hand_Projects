package com.twohands.notification_service.integration.settings;

import com.twohands.notification_service.application.settings.InitializeDefaultNotificationSettingsCommand;
import com.twohands.notification_service.application.settings.InitializeDefaultNotificationSettingsUseCase;
import com.twohands.notification_service.application.worker.ProcessNotificationEventCommand;
import com.twohands.notification_service.application.worker.ProcessNotificationEventOutcome;
import com.twohands.notification_service.application.worker.ProcessNotificationEventUseCase;
import com.twohands.notification_service.domain.delivery.NotificationDefaultChannelPolicy;
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

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@SpringBootTest
@ActiveProfiles("test")
class InitializeDefaultNotificationSettingsIntegrationTest {

    @Autowired
    private InitializeDefaultNotificationSettingsUseCase initializeDefaultNotificationSettingsUseCase;

    @Autowired
    private ProcessNotificationEventUseCase processNotificationEventUseCase;

    @Autowired
    private NotificationEventRepository notificationEventRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void cleanTables() {
        jdbcTemplate.execute("DELETE FROM user_notification_settings");
        jdbcTemplate.execute("DELETE FROM user_notifications");
        jdbcTemplate.execute("DELETE FROM notification_events");
    }

    @Test
    void execute_createsDefaultSettingsForAllSupportedEventTypes() {
        UUID userId = UUID.randomUUID();

        var result = initializeDefaultNotificationSettingsUseCase.execute(
                new InitializeDefaultNotificationSettingsCommand(userId)
        );

        assertEquals(NotificationDefaultChannelPolicy.supportedEventTypes().size(), result.createdCount());
        assertEquals(0, result.skippedCount());
        assertEquals(
                NotificationDefaultChannelPolicy.supportedEventTypes().size(),
                countSettingsForUser(userId)
        );
    }

    @Test
    void execute_isIdempotentAndDoesNotOverwriteExplicitSettings() {
        UUID userId = UUID.randomUUID();
        jdbcTemplate.update(
                """
                        INSERT INTO user_notification_settings(
                            user_id, event_type, allow_push, allow_email, allow_in_app, created_at, updated_at
                        )
                        VALUES (?, 'POST_LIKED', FALSE, FALSE, FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                        """,
                userId
        );

        var first = initializeDefaultNotificationSettingsUseCase.execute(
                new InitializeDefaultNotificationSettingsCommand(userId)
        );
        var second = initializeDefaultNotificationSettingsUseCase.execute(
                new InitializeDefaultNotificationSettingsCommand(userId)
        );

        assertEquals(NotificationDefaultChannelPolicy.supportedEventTypes().size() - 1, first.createdCount());
        assertEquals(0, second.createdCount());
        assertFalse(queryAllowInApp(userId, "POST_LIKED"));
        assertEquals(
                NotificationDefaultChannelPolicy.supportedEventTypes().size(),
                countSettingsForUser(userId)
        );
    }

    @Test
    void processUserCreatedEvent_initializesSettingsAndCompletesEvent() {
        UUID userId = UUID.randomUUID();
        UUID eventId = UUID.randomUUID();
        notificationEventRepository.save(new NotificationEvent(
                eventId,
                UUID.randomUUID(),
                null,
                "USER_CREATED",
                NotificationSourceService.AUTH,
                null,
                null,
                null,
                userId,
                "{\"user_id\":\"" + userId + "\"}",
                NotificationEventStatus.PROCESSING,
                0,
                5,
                null,
                Instant.now(),
                "worker-1",
                Instant.now(),
                null
        ));

        var result = processNotificationEventUseCase.execute(new ProcessNotificationEventCommand(eventId));

        assertEquals(ProcessNotificationEventOutcome.COMPLETED, result.outcome());
        assertEquals("COMPLETED", queryEventStatus(eventId));
        assertEquals(
                NotificationDefaultChannelPolicy.supportedEventTypes().size(),
                countSettingsForUser(userId)
        );
    }

    private int countSettingsForUser(UUID userId) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM user_notification_settings WHERE user_id = ?",
                Integer.class,
                userId
        );
        return count == null ? 0 : count;
    }

    private boolean queryAllowInApp(UUID userId, String eventType) {
        Boolean value = jdbcTemplate.queryForObject(
                """
                        SELECT allow_in_app FROM user_notification_settings
                        WHERE user_id = ? AND event_type = ?
                        """,
                Boolean.class,
                userId,
                eventType
        );
        return Boolean.TRUE.equals(value);
    }

    private String queryEventStatus(UUID eventId) {
        return jdbcTemplate.queryForObject(
                "SELECT status FROM notification_events WHERE id = ?",
                String.class,
                eventId
        );
    }
}
