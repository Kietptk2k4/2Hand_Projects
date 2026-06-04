package com.twohands.auth_service.unit.infrastructure.outbox;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.twohands.auth_service.domain.outbox.OutboxEvent;
import com.twohands.auth_service.domain.outbox.OutboxStatus;
import com.twohands.auth_service.infrastructure.outbox.AuthOutboxEventKeyResolver;
import com.twohands.auth_service.infrastructure.outbox.AuthOutboxMessageBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuthOutboxMessageBuilderTest {

    private AuthOutboxMessageBuilder messageBuilder;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        messageBuilder = new AuthOutboxMessageBuilder(objectMapper, new AuthOutboxEventKeyResolver(objectMapper));
    }

    @Test
    void buildEnvelopeShouldIncludeRequiredFields() {
        UUID outboxId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Instant now = Instant.parse("2026-06-04T08:00:00Z");
        String payloadJson = """
                {"user_id":"%s","email":"user@example.com"}
                """.formatted(userId);

        OutboxEvent event = new OutboxEvent(
                outboxId,
                "USER_CREATED",
                "auth-service",
                payloadJson,
                OutboxStatus.PENDING,
                0,
                now,
                null,
                null
        );

        Map<String, Object> envelope = messageBuilder.buildEnvelope(event);

        assertEquals(outboxId.toString(), envelope.get("event_id"));
        assertEquals("USER_CREATED", envelope.get("event_type"));
        assertEquals("auth.user.created:" + userId, envelope.get("event_key"));
        assertEquals("auth", envelope.get("source"));
        assertEquals(now.toString(), envelope.get("occurred_at"));
        assertTrue(envelope.get("payload") instanceof Map);
    }
}
