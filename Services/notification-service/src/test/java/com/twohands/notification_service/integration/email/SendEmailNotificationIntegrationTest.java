package com.twohands.notification_service.integration.email;

import com.twohands.notification_service.application.email.SendEmailNotificationCommand;
import com.twohands.notification_service.application.email.SendEmailNotificationOutcome;
import com.twohands.notification_service.application.email.SendEmailNotificationUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = "notification.integrations.email.enabled=true")
class SendEmailNotificationIntegrationTest {

    @Autowired
    private SendEmailNotificationUseCase sendEmailNotificationUseCase;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void cleanTables() {
        jdbcTemplate.execute("DELETE FROM user_notification_settings");
    }

    @Test
    void execute_sendsVerificationEmailWhenEnabledAndPayloadValid() {
        UUID userId = UUID.randomUUID();

        var result = sendEmailNotificationUseCase.execute(new SendEmailNotificationCommand(
                userId,
                "EMAIL_VERIFICATION_REQUESTED",
                """
                        {
                          "recipient_email": "user@example.com",
                          "verification_link": "https://2hands.vn/verify?token=abc"
                        }
                        """
        ));

        assertEquals(SendEmailNotificationOutcome.SENT, result.outcome());
    }

    @Test
    void execute_skipsNonEmailEventType() {
        UUID userId = UUID.randomUUID();

        var result = sendEmailNotificationUseCase.execute(new SendEmailNotificationCommand(
                userId,
                "POST_LIKED",
                """
                        {"recipient_email":"user@example.com"}
                        """
        ));

        assertEquals(SendEmailNotificationOutcome.SKIPPED, result.outcome());
    }

    @Test
    void execute_respectsDisabledEmailSettingUnlessCriticalOverride() {
        UUID userId = UUID.randomUUID();
        jdbcTemplate.update(
                """
                        INSERT INTO user_notification_settings(
                            user_id, event_type, allow_push, allow_email, allow_in_app, created_at, updated_at
                        )
                        VALUES (?, 'ORDER_CREATED', TRUE, FALSE, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                        """,
                userId
        );

        var orderResult = sendEmailNotificationUseCase.execute(new SendEmailNotificationCommand(
                userId,
                "ORDER_CREATED",
                """
                        {"recipient_email":"buyer@example.com","order_code":"ORD-1"}
                        """
        ));
        assertEquals(SendEmailNotificationOutcome.SKIPPED, orderResult.outcome());

        var suspendedResult = sendEmailNotificationUseCase.execute(new SendEmailNotificationCommand(
                userId,
                "USER_SUSPENDED",
                """
                        {"recipient_email":"user@example.com"}
                        """
        ));
        assertEquals(SendEmailNotificationOutcome.SENT, suspendedResult.outcome());
    }
}
