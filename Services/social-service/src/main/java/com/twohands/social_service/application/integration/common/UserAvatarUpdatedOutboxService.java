package com.twohands.social_service.application.integration.common;

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
public class UserAvatarUpdatedOutboxService {

    private static final String USER_AVATAR_UPDATED_EVENT_TYPE = "USER_AVATAR_UPDATED";

    private final ObjectMapper objectMapper;

    public UserAvatarUpdatedOutboxService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public OutboxEvent build(
            UUID userId,
            String displayName,
            String avatarUrl,
            List<UUID> followerUserIds,
            Instant now
    ) {
        Map<String, Object> payload = new HashMap<>();
        String user = userId.toString();
        payload.put("actor_id", user);
        payload.put("user_id", user);
        payload.put("avatar_url", avatarUrl);
        putIfPresent(payload, "display_name", displayName);
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
                USER_AVATAR_UPDATED_EVENT_TYPE,
                user,
                payloadJson,
                OutboxStatus.PENDING,
                0,
                now,
                null,
                null
        );
    }

    private void putIfPresent(Map<String, Object> payload, String key, String value) {
        if (value != null && !value.isBlank()) {
            payload.put(key, value);
        }
    }
}
