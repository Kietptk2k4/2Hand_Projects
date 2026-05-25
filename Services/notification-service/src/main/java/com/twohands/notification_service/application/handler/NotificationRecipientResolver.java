package com.twohands.notification_service.application.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.twohands.notification_service.domain.notificationevent.NotificationEvent;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Component
public class NotificationRecipientResolver {

    private final ObjectMapper objectMapper;

    public NotificationRecipientResolver(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public List<UUID> resolve(NotificationEvent event) {
        Set<UUID> recipients = new LinkedHashSet<>();
        if (event.recipientUserId() != null) {
            recipients.add(event.recipientUserId());
        }

        try {
            JsonNode payload = objectMapper.readTree(event.payload());
            addRecipient(recipients, payload, "recipient_user_id");
            addRecipient(recipients, payload, "followed_user_id");
            addRecipient(recipients, payload, "user_id");
            addRecipient(recipients, payload, "post_author_id");
            addRecipient(recipients, payload, "post_owner_id");
            addRecipient(recipients, payload, "parent_comment_author_id");
            addRecipient(recipients, payload, "comment_owner_id");

            JsonNode recipientIds = payload.get("recipient_user_ids");
            if (recipientIds != null && recipientIds.isArray()) {
                for (JsonNode node : recipientIds) {
                    if (node != null && node.isTextual()) {
                        addRecipient(recipients, node.asText());
                    }
                }
            }
        } catch (Exception ignored) {
            // Payload parsing issues are handled by caller when recipients remain empty.
        }

        return new ArrayList<>(recipients);
    }

    private void addRecipient(Set<UUID> recipients, JsonNode payload, String field) {
        JsonNode node = payload.get(field);
        if (node != null && node.isTextual()) {
            addRecipient(recipients, node.asText());
        }
    }

    private void addRecipient(Set<UUID> recipients, String rawValue) {
        if (rawValue == null || rawValue.isBlank()) {
            return;
        }
        try {
            recipients.add(UUID.fromString(rawValue));
        } catch (IllegalArgumentException ignored) {
            // Ignore invalid UUID values in payload.
        }
    }
}
