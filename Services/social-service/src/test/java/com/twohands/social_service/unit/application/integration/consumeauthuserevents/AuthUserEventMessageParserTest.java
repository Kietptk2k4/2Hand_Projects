package com.twohands.social_service.unit.application.integration.consumeauthuserevents;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.twohands.social_service.application.integration.consumeauthuserevents.AuthUserEventMessageParser;
import com.twohands.social_service.application.integration.consumeauthuserevents.AuthUserEventType;
import com.twohands.social_service.application.integration.consumeauthuserevents.ConsumeAuthUserEventCommand;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class AuthUserEventMessageParserTest {

    private final AuthUserEventMessageParser parser = new AuthUserEventMessageParser(new ObjectMapper());

    @Test
    void shouldParseFlatUserCreatedPayload() {
        UUID eventId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        String json = """
                {
                  "event_id": "%s",
                  "event_type": "USER_CREATED",
                  "user_id": "%s",
                  "status": "ACTIVE",
                  "display_name": "User A",
                  "avatar_url": "https://cdn.2hands.vn/a.png",
                  "is_private": false
                }
                """.formatted(eventId, userId);

        ConsumeAuthUserEventCommand command = parser.parse(json, "auth.user.created", null);

        assertThat(command.eventId()).isEqualTo(eventId);
        assertThat(command.eventType()).isEqualTo(AuthUserEventType.USER_CREATED);
        assertThat(command.userId()).isEqualTo(userId);
        assertThat(command.displayName()).isEqualTo("User A");
    }

    @Test
    void shouldParseEnforcementPayloadUsingTopicFallback() {
        UUID eventId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        String json = """
                {
                  "event_id": "%s",
                  "user_id": "%s",
                  "enforcement_id": "%s",
                  "action_type": "SUSPEND"
                }
                """.formatted(eventId, userId, UUID.randomUUID());

        ConsumeAuthUserEventCommand command = parser.parse(json, "admin.user.suspended", "USER_SUSPENDED");

        assertThat(command.eventType()).isEqualTo(AuthUserEventType.USER_SUSPENDED);
        assertThat(command.userId()).isEqualTo(userId);
    }

    @Test
    void shouldParseAdminOutboxEnvelopeWithNestedPayload() {
        UUID eventId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID enforcementId = UUID.randomUUID();

        String json = """
                {
                  "event_id": "%s",
                  "event_type": "USER_SUSPENDED",
                  "source": "admin",
                  "occurred_at": "2026-06-04T10:00:00Z",
                  "payload": {
                    "user_id": "%s",
                    "enforcement_id": "%s",
                    "action_type": "SUSPEND",
                    "reason_code": "POLICY_VIOLATION",
                    "status": "ACTIVE"
                  }
                }
                """.formatted(eventId, userId, enforcementId);

        ConsumeAuthUserEventCommand command = parser.parse(json, "admin.user.suspended", null);

        assertThat(command.eventId()).isEqualTo(eventId);
        assertThat(command.eventType()).isEqualTo(AuthUserEventType.USER_SUSPENDED);
        assertThat(command.userId()).isEqualTo(userId);
    }

    @Test
    void shouldParseEnforcementRevokedEnvelope() {
        UUID eventId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        String json = """
                {
                  "event_id": "%s",
                  "event_type": "USER_ENFORCEMENT_REVOKED",
                  "payload": {
                    "user_id": "%s",
                    "enforcement_id": "%s"
                  }
                }
                """.formatted(eventId, userId, UUID.randomUUID());

        ConsumeAuthUserEventCommand command = parser.parse(json, "admin.user.enforcement_revoked", null);

        assertThat(command.eventType()).isEqualTo(AuthUserEventType.USER_ENFORCEMENT_REVOKED);
        assertThat(command.userId()).isEqualTo(userId);
    }
}
