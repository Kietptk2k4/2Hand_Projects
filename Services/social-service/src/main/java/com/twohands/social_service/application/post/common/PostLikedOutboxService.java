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
import java.util.Map;
import java.util.UUID;

@Component
public class PostLikedOutboxService {

    private static final String POST_LIKED_EVENT_TYPE = "POST_LIKED";

    private final ObjectMapper objectMapper;

    public PostLikedOutboxService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public OutboxEvent build(String postId, UUID actorId, String postAuthorId, Instant now) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("post_id", postId);
        String actor = actorId.toString();
        payload.put("actor_id", actor);
        payload.put("user_id", actor);
        putIfPresent(payload, "post_author_id", postAuthorId);

        return toOutboxEvent(postId, payload, now);
    }

    private void putIfPresent(Map<String, Object> payload, String key, String value) {
        if (value != null && !value.isBlank()) {
            payload.put(key, value);
        }
    }

    private OutboxEvent toOutboxEvent(String aggregateId, Map<String, Object> payload, Instant now) {
        String payloadJson;
        try {
            payloadJson = objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException ex) {
            throw new AppException(ErrorCode.INTERNAL_ERROR, "Cannot serialize outbox payload", ex);
        }

        return new OutboxEvent(
                UUID.randomUUID(),
                POST_LIKED_EVENT_TYPE,
                aggregateId,
                payloadJson,
                OutboxStatus.PENDING,
                0,
                now,
                null,
                null
        );
    }
}
