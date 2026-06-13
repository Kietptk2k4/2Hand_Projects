package com.twohands.social_service.application.post.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.twohands.social_service.domain.outbox.OutboxEvent;
import com.twohands.social_service.domain.outbox.OutboxStatus;
import com.twohands.social_service.exception.AppException;
import com.twohands.social_service.exception.ErrorCode;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
public class PostCreatedOutboxService {

    private static final String POST_CREATED_EVENT_TYPE = "POST_CREATED";
    private static final int CAPTION_PREVIEW_MAX_LENGTH = 120;

    private final ObjectMapper objectMapper;

    public PostCreatedOutboxService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public OutboxEvent build(
            String postId,
            UUID authorId,
            String actorDisplayName,
            String visibility,
            String caption,
            List<UUID> followerUserIds,
            Instant now
    ) {
        Map<String, Object> payload = new HashMap<>();
        String author = authorId.toString();
        payload.put("post_id", postId);
        payload.put("actor_id", author);
        payload.put("user_id", author);
        payload.put("post_author_id", author);
        putIfPresent(payload, "actor_display_name", actorDisplayName);
        putIfPresent(payload, "visibility", visibility);
        putIfPresent(payload, "caption_preview", captionPreview(caption));
        payload.put(
                "follower_user_ids",
                followerUserIds.stream().map(UUID::toString).toList()
        );

        String payloadJson;
        try {
            payloadJson = objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException ex) {
            throw new AppException(ErrorCode.INTERNAL_ERROR, "Cannot serialize outbox payload", ex);
        }

        return new OutboxEvent(
                UUID.randomUUID(),
                POST_CREATED_EVENT_TYPE,
                postId,
                payloadJson,
                OutboxStatus.PENDING,
                0,
                now,
                null,
                null
        );
    }

    private String captionPreview(String caption) {
        if (caption == null || caption.isBlank()) {
            return null;
        }
        String trimmed = caption.trim();
        if (trimmed.length() <= CAPTION_PREVIEW_MAX_LENGTH) {
            return trimmed;
        }
        return trimmed.substring(0, CAPTION_PREVIEW_MAX_LENGTH);
    }

    private void putIfPresent(Map<String, Object> payload, String key, String value) {
        if (value != null && !value.isBlank()) {
            payload.put(key, value);
        }
    }
}
