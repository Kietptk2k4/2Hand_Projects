package com.twohands.notification_service.application.delivery;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.twohands.notification_service.application.worker.NotificationFailurePolicy;
import com.twohands.notification_service.domain.delivery.EmailDeliveryRetryState;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Optional;

@Component
public class EmailDeliveryRetryMetadataCodec {

    private static final String EMAIL_DELIVERY_KEY = "emailDelivery";
    private static final String FAILURE_POLICY_KEY = "failurePolicy";
    private static final String RETRY_COUNT_KEY = "retryCount";
    private static final String MAX_RETRY_COUNT_KEY = "maxRetryCount";
    private static final String LAST_ERROR_KEY = "lastError";
    private static final String LAST_ATTEMPT_AT_KEY = "lastAttemptAt";

    private final ObjectMapper objectMapper;

    public EmailDeliveryRetryMetadataCodec(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public Optional<EmailDeliveryRetryState> parse(String metadataJson) {
        if (metadataJson == null || metadataJson.isBlank()) {
            return Optional.empty();
        }
        try {
            JsonNode root = objectMapper.readTree(metadataJson);
            JsonNode emailDelivery = root.get(EMAIL_DELIVERY_KEY);
            if (emailDelivery == null || emailDelivery.isNull()) {
                return Optional.empty();
            }
            NotificationFailurePolicy failurePolicy = NotificationFailurePolicy.valueOf(
                    emailDelivery.get(FAILURE_POLICY_KEY).asText()
            );
            int retryCount = emailDelivery.get(RETRY_COUNT_KEY).asInt();
            int maxRetryCount = emailDelivery.get(MAX_RETRY_COUNT_KEY).asInt();
            String lastError = emailDelivery.hasNonNull(LAST_ERROR_KEY)
                    ? emailDelivery.get(LAST_ERROR_KEY).asText()
                    : null;
            Instant lastAttemptAt = emailDelivery.hasNonNull(LAST_ATTEMPT_AT_KEY)
                    ? Instant.parse(emailDelivery.get(LAST_ATTEMPT_AT_KEY).asText())
                    : null;
            return Optional.of(new EmailDeliveryRetryState(
                    failurePolicy,
                    retryCount,
                    maxRetryCount,
                    lastError,
                    lastAttemptAt
            ));
        } catch (Exception ex) {
            return Optional.empty();
        }
    }

    public String mergeEmailDeliveryState(String metadataJson, EmailDeliveryRetryState state) {
        ObjectNode root = readRootObject(metadataJson);
        ObjectNode emailDelivery = objectMapper.createObjectNode();
        emailDelivery.put(FAILURE_POLICY_KEY, state.failurePolicy().name());
        emailDelivery.put(RETRY_COUNT_KEY, state.retryCount());
        emailDelivery.put(MAX_RETRY_COUNT_KEY, state.maxRetryCount());
        if (state.lastError() != null) {
            emailDelivery.put(LAST_ERROR_KEY, state.lastError());
        }
        if (state.lastAttemptAt() != null) {
            emailDelivery.put(LAST_ATTEMPT_AT_KEY, state.lastAttemptAt().toString());
        }
        root.set(EMAIL_DELIVERY_KEY, emailDelivery);
        return writeRoot(root);
    }

    public String clearEmailDeliveryState(String metadataJson) {
        ObjectNode root = readRootObject(metadataJson);
        root.remove(EMAIL_DELIVERY_KEY);
        return writeRoot(root);
    }

    private ObjectNode readRootObject(String metadataJson) {
        if (metadataJson == null || metadataJson.isBlank()) {
            return objectMapper.createObjectNode();
        }
        try {
            JsonNode root = objectMapper.readTree(metadataJson);
            if (root instanceof ObjectNode objectNode) {
                return objectNode;
            }
        } catch (Exception ignored) {
            // fall through
        }
        return objectMapper.createObjectNode();
    }

    private String writeRoot(ObjectNode root) {
        try {
            return objectMapper.writeValueAsString(root);
        } catch (Exception ex) {
            return "{}";
        }
    }
}
