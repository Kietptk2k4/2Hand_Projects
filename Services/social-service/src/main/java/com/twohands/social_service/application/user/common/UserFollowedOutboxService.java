package com.twohands.social_service.application.user.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.twohands.social_service.domain.follow.FollowStatus;
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
public class UserFollowedOutboxService {

    private static final String USER_FOLLOWED_EVENT_TYPE = "USER_FOLLOWED";

    private final ObjectMapper objectMapper;

    public UserFollowedOutboxService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public OutboxEvent build(UUID followerId, UUID followeeId, FollowStatus status, Instant now) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("follower_id", followerId.toString());
        payload.put("followee_id", followeeId.toString());
        payload.put("status", status.name());

        String payloadJson;
        try {
            payloadJson = objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException ex) {
            throw new AppException(ErrorCode.INTERNAL_ERROR, "Cannot serialize outbox payload", ex);
        }

        return new OutboxEvent(
                UUID.randomUUID(),
                USER_FOLLOWED_EVENT_TYPE,
                followeeId.toString(),
                payloadJson,
                OutboxStatus.PENDING,
                0,
                now,
                null
        );
    }
}
