package com.twohands.auth_service.application.auth.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.twohands.auth_service.application.useraccount.common.UserProjectionSyncPayload;
import com.twohands.auth_service.domain.outbox.OutboxEvent;
import com.twohands.auth_service.domain.outbox.OutboxStatus;
import com.twohands.auth_service.exception.AppException;
import com.twohands.auth_service.exception.ErrorCode;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
public class UserCreatedOutboxService {

    private static final String SOURCE = "auth-service";
    private static final String USER_CREATED_EVENT_TYPE = "USER_CREATED";

    private final ObjectMapper objectMapper;

    public UserCreatedOutboxService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public OutboxEvent build(UUID userId, String email, String status, Instant now) {
        return build(userId, email, status, now, UserProjectionSyncPayload.empty());
    }

    public OutboxEvent build(UUID userId, String email, String status, Instant now, UserProjectionSyncPayload sync) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("user_id", userId.toString());
        payload.put("email", email);
        if (status != null && !status.isBlank()) {
            payload.put("status", status);
        }
        if (sync != null) {
            sync.applyTo(payload);
        }

        String payloadJson;
        try {
            payloadJson = objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException ex) {
            throw new AppException(ErrorCode.INTERNAL_ERROR, "Cannot serialize outbox payload", ex);
        }

        return new OutboxEvent(
                UUID.randomUUID(),
                USER_CREATED_EVENT_TYPE,
                SOURCE,
                payloadJson,
                OutboxStatus.PENDING,
                0,
                now,
                null,
                null
        );
    }
}
