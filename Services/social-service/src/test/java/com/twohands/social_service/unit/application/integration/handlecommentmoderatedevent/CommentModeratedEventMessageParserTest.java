package com.twohands.social_service.unit.application.integration.handlecommentmoderatedevent;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.twohands.social_service.application.integration.handlecommentmoderatedevent.CommentModeratedEventMessageParser;
import com.twohands.social_service.application.integration.handlecommentmoderatedevent.HandleCommentModeratedEventCommand;
import com.twohands.social_service.domain.comment.CommentModerationAction;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class CommentModeratedEventMessageParserTest {

    private final CommentModeratedEventMessageParser parser =
            new CommentModeratedEventMessageParser(new ObjectMapper());

    @Test
    void shouldParseEnvelopePayload() {
        UUID eventId = UUID.randomUUID();
        UUID moderationLogId = UUID.randomUUID();
        String raw = """
                {
                  "event_id": "%s",
                  "payload": {
                    "comment_id": "507f1f77bcf86cd799439011",
                    "moderation_log_id": "%s",
                    "action": "HIDE",
                    "reason": "Spam",
                    "moderated_by": "%s",
                    "moderated_at": "2026-05-23T10:00:00Z"
                  }
                }
                """.formatted(eventId, moderationLogId, UUID.randomUUID());

        HandleCommentModeratedEventCommand command = parser.parse(raw);

        assertThat(command.eventId()).isEqualTo(eventId);
        assertThat(command.commentId()).isEqualTo("507f1f77bcf86cd799439011");
        assertThat(command.moderationLogId()).isEqualTo(moderationLogId);
        assertThat(command.action()).isEqualTo(CommentModerationAction.HIDE);
        assertThat(command.reason()).isEqualTo("Spam");
    }
}
