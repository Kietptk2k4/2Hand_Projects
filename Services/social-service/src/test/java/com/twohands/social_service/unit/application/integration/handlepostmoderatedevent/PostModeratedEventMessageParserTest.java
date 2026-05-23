package com.twohands.social_service.unit.application.integration.handlepostmoderatedevent;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.twohands.social_service.application.integration.handlepostmoderatedevent.HandlePostModeratedEventCommand;
import com.twohands.social_service.application.integration.handlepostmoderatedevent.InvalidPostModeratedEventException;
import com.twohands.social_service.application.integration.handlepostmoderatedevent.PostModeratedEventMessageParser;
import com.twohands.social_service.domain.post.PostModerationAction;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PostModeratedEventMessageParserTest {

    private final PostModeratedEventMessageParser parser = new PostModeratedEventMessageParser(new ObjectMapper());

    @Test
    void shouldParseEnvelopeMessage() {
        UUID eventId = UUID.randomUUID();
        UUID moderationLogId = UUID.randomUUID();
        String json = """
                {
                  "event_id": "%s",
                  "event_type": "POST_MODERATED",
                  "occurred_at": "2026-05-23T10:00:00Z",
                  "payload": {
                    "post_id": "507f1f77bcf86cd799439011",
                    "moderation_log_id": "%s",
                    "action": "HIDE",
                    "reason": "Spam",
                    "moderated_by": "550e8400-e29b-41d4-a716-446655440099",
                    "moderated_at": "2026-05-23T10:00:00Z"
                  }
                }
                """.formatted(eventId, moderationLogId);

        HandlePostModeratedEventCommand command = parser.parse(json);

        assertThat(command.eventId()).isEqualTo(eventId);
        assertThat(command.postId()).isEqualTo("507f1f77bcf86cd799439011");
        assertThat(command.moderationLogId()).isEqualTo(moderationLogId);
        assertThat(command.action()).isEqualTo(PostModerationAction.HIDE);
        assertThat(command.reason()).isEqualTo("Spam");
    }

    @Test
    void shouldRejectUnknownAction() {
        String json = """
                {
                  "event_id": "550e8400-e29b-41d4-a716-446655440001",
                  "payload": {
                    "post_id": "507f1f77bcf86cd799439011",
                    "moderation_log_id": "550e8400-e29b-41d4-a716-446655440002",
                    "action": "RESTORE"
                  }
                }
                """;

        assertThatThrownBy(() -> parser.parse(json))
                .isInstanceOf(InvalidPostModeratedEventException.class);
    }
}
