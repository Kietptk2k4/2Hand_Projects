package com.twohands.auth_service.application.auth.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.twohands.auth_service.domain.outbox.OutboxEvent;
import com.twohands.auth_service.domain.outbox.OutboxStatus;
import com.twohands.auth_service.domain.user.User;
import com.twohands.auth_service.domain.user.VerificationTokenType;
import com.twohands.auth_service.exception.AppException;
import com.twohands.auth_service.exception.ErrorCode;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
public class EmailVerificationOutboxService {

    private static final String SOURCE = "auth-service";
    private static final String EVENT_TYPE = "EMAIL_VERIFICATION_REQUESTED";

    private final ObjectMapper objectMapper;

    public EmailVerificationOutboxService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * OTP-only email verify payload. {@code verification_code} is the canonical field for Notification.
     * {@code verification_token} duplicates the same 6-digit OTP for Kafka/envelope backward compatibility.
     */
    public OutboxEvent build(User user, String verificationOtpRaw, Instant now) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("user_id", user.id().toString());
        payload.put("email", user.email().normalizedValue());
        payload.put("verification_code", verificationOtpRaw);
        payload.put("verification_token", verificationOtpRaw);
        payload.put("verification_token_type", VerificationTokenType.EMAIL_VERIFY.name());

        String payloadJson;
        try {
            payloadJson = objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException ex) {
            throw new AppException(ErrorCode.INTERNAL_ERROR, "Cannot serialize outbox payload", ex);
        }

        return new OutboxEvent(
                UUID.randomUUID(),
                EVENT_TYPE,
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
