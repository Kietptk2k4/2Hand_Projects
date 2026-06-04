package com.twohands.auth_service.application.useraccount.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
public class UserAccountOutboxService {

    private static final String SOURCE = "auth-service";

    private final ObjectMapper objectMapper;

    public UserAccountOutboxService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public OutboxEvent userUpdated(UUID userId, String email, Instant now) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("user_id", userId.toString());
        payload.put("email", email);
        payload.put("updated_at", now.toString());
        return build("USER_UPDATED", payload, now);
    }

    public OutboxEvent userActivatedAfterEmailVerification(UUID userId, String email, Instant now) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("user_id", userId.toString());
        payload.put("email", email);
        payload.put("status", "ACTIVE");
        payload.put("email_verified", true);
        payload.put("verified_at", now.toString());
        return build("USER_UPDATED", payload, now);
    }

    public OutboxEvent userDeleted(UUID userId, String email, Instant now) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("user_id", userId.toString());
        payload.put("email", email);
        payload.put("deleted_at", now.toString());
        return build("USER_DELETED", payload, now);
    }

    private OutboxEvent build(String eventType, Map<String, Object> payload, Instant now) {
        String payloadJson;
        try {
            payloadJson = objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException ex) {
            throw new AppException(ErrorCode.INTERNAL_ERROR, "Cannot serialize outbox payload", ex);
        }

        return new OutboxEvent(
                UUID.randomUUID(),
                eventType,
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
