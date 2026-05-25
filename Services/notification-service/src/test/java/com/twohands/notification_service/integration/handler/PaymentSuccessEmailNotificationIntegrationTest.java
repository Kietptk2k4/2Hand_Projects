package com.twohands.notification_service.integration.handler;

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

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = "notification.integrations.email.enabled=true")
class PaymentSuccessEmailNotificationIntegrationTest {

    @Autowired
    private ProcessNotificationEventUseCase processNotificationEventUseCase;

    @Autowired
    private NotificationEventRepository notificationEventRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void cleanTables() {
        jdbcTemplate.execute("DELETE FROM user_notifications");
        jdbcTemplate.execute("DELETE FROM notification_events");
    }

    @Test
    void process_completesPaymentSuccessEmailFlowForBuyer() {
        UUID buyerId = UUID.randomUUID();
        UUID eventId = insertPaymentSuccessEvent(buyerId);

        var result = processNotificationEventUseCase.execute(new ProcessNotificationEventCommand(eventId));

        assertEquals(ProcessNotificationEventOutcome.COMPLETED, result.outcome());
        assertEquals("COMPLETED", queryEventStatus(eventId));
        assertEquals(1, countNotifications(eventId, buyerId));
    }

    @Test
    void process_marksFailedWhenRecipientEmailMissing() {
        UUID buyerId = UUID.randomUUID();
        UUID eventId = insertPaymentSuccessEventWithoutEmail(buyerId);

        var result = processNotificationEventUseCase.execute(new ProcessNotificationEventCommand(eventId));

        assertEquals(ProcessNotificationEventOutcome.FAILED, result.outcome());
        assertEquals("FAILED", queryEventStatus(eventId));
    }

    private UUID insertPaymentSuccessEvent(UUID buyerId) {
        UUID eventId = UUID.randomUUID();
        String payload = """
                {
                  "buyer_id":"%s",
                  "payment_id":"pay-100",
                  "order_id":"order-100",
                  "order_code":"ORD-100",
                  "recipient_email":"buyer@example.com",
                  "amount":"100000",
                  "payment_method":"COD"
                }
                """.formatted(buyerId);

        notificationEventRepository.save(new NotificationEvent(
                eventId,
                UUID.randomUUID(),
                null,
                "PAYMENT_SUCCESS",
                NotificationSourceService.COMMERCE,
                "PAYMENT",
                "pay-100",
                null,
                buyerId,
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

    private UUID insertPaymentSuccessEventWithoutEmail(UUID buyerId) {
        UUID eventId = UUID.randomUUID();
        String payload = """
                {
                  "buyer_id":"%s",
                  "payment_id":"pay-100",
                  "order_id":"order-100",
                  "order_code":"ORD-100",
                  "amount":"100000"
                }
                """.formatted(buyerId);

        notificationEventRepository.save(new NotificationEvent(
                eventId,
                UUID.randomUUID(),
                null,
                "PAYMENT_SUCCESS",
                NotificationSourceService.COMMERCE,
                "PAYMENT",
                "pay-100",
                null,
                buyerId,
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

    private String queryEventStatus(UUID eventId) {
        return jdbcTemplate.queryForObject(
                "SELECT status FROM notification_events WHERE id = ?",
                String.class,
                eventId
        );
    }
}
