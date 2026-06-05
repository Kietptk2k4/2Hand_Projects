package com.twohands.notification_service.integration.consume;

import com.twohands.notification_service.application.consume.ConsumeDomainEventUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@SpringBootTest
@ActiveProfiles("test")
class ConsumeDomainEventIntegrationTest {

    @Autowired
    private ConsumeDomainEventUseCase consumeDomainEventUseCase;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void cleanTable() {
        jdbcTemplate.execute("DELETE FROM notification_events");
    }

    @Test
    void execute_storesPendingAuthEmailVerificationFromKafkaEnvelope() {
        UUID eventId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        String message = """
                {
                  "event_id": "%s",
                  "event_type": "EMAIL_VERIFICATION_REQUESTED",
                  "event_key": "%s",
                  "source": "auth",
                  "occurred_at": "2026-06-05T10:33:44.808Z",
                  "payload": {
                    "user_id": "%s",
                    "email": "user@example.com",
                    "verification_code": "123456",
                    "verification_token": "123456",
                    "verification_token_type": "EMAIL_VERIFY"
                  }
                }
                """.formatted(eventId, eventId, userId);

        var result = consumeDomainEventUseCase.execute(message, "auth.email.verification_requested");

        assertFalse(result.duplicate());
        assertEquals(1, countEvents());
        assertEquals("PENDING", queryStatus(eventId));
        assertEquals("EMAIL_VERIFICATION_REQUESTED", queryEventType(eventId));
    }

    @Test
    void execute_storesPendingEventFromBrokerEnvelope() {
        UUID eventId = UUID.randomUUID();
        String message = """
                {
                  "event_id": "%s",
                  "event_type": "POST_LIKED",
                  "source_service": "SOCIAL",
                  "event_key": "social.post.post-id.liked",
                  "payload": {"post_id":"post-id"}
                }
                """.formatted(eventId);

        var first = consumeDomainEventUseCase.execute(message, "social.post.liked");
        var second = consumeDomainEventUseCase.execute(message, "social.post.liked");

        assertFalse(first.duplicate());
        assertEquals(first.notificationEventId(), second.notificationEventId());
        assertEquals(1, countEvents());
        assertEquals("PENDING", queryStatus(eventId));
        assertEquals("POST_LIKED", queryEventType(eventId));
    }

    private int countEvents() {
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM notification_events", Integer.class);
        return count == null ? 0 : count;
    }

    private String queryStatus(UUID sourceEventId) {
        return jdbcTemplate.queryForObject(
                "SELECT status FROM notification_events WHERE source_event_id = ?",
                String.class,
                sourceEventId
        );
    }

    private String queryEventType(UUID sourceEventId) {
        return jdbcTemplate.queryForObject(
                "SELECT event_type FROM notification_events WHERE source_event_id = ?",
                String.class,
                sourceEventId
        );
    }
}
