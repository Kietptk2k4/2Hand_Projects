package com.twohands.notification_service.application.delivery;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.twohands.notification_service.application.worker.NotificationFailurePolicy;
import com.twohands.notification_service.domain.delivery.PushDeliveryRetryState;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Optional;

@Component
public class PushDeliveryRetryMetadataCodec {

    private static final String PUSH_DELIVERY_KEY = "pushDelivery";
    private static final String FAILURE_POLICY_KEY = "failurePolicy";
    private static final String RETRY_COUNT_KEY = "retryCount";
    private static final String MAX_RETRY_COUNT_KEY = "maxRetryCount";
    private static final String LAST_ERROR_KEY = "lastError";
    private static final String LAST_ATTEMPT_AT_KEY = "lastAttemptAt";

    private final ObjectMapper objectMapper;

    public PushDeliveryRetryMetadataCodec(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public Optional<PushDeliveryRetryState> parse(String metadataJson) {
        if (metadataJson == null || metadataJson.isBlank()) {
            return Optional.empty();
        }
        try {
            JsonNode root = objectMapper.readTree(metadataJson);
            JsonNode pushDelivery = root.get(PUSH_DELIVERY_KEY);
            if (pushDelivery == null || pushDelivery.isNull()) {
                return Optional.empty();
            }
            NotificationFailurePolicy failurePolicy = NotificationFailurePolicy.valueOf(
                    pushDelivery.get(FAILURE_POLICY_KEY).asText()
            );
            int retryCount = pushDelivery.get(RETRY_COUNT_KEY).asInt();
            int maxRetryCount = pushDelivery.get(MAX_RETRY_COUNT_KEY).asInt();
            String lastError = pushDelivery.hasNonNull(LAST_ERROR_KEY)
                    ? pushDelivery.get(LAST_ERROR_KEY).asText()
                    : null;
            Instant lastAttemptAt = pushDelivery.hasNonNull(LAST_ATTEMPT_AT_KEY)
                    ? Instant.parse(pushDelivery.get(LAST_ATTEMPT_AT_KEY).asText())
                    : null;
            return Optional.of(new PushDeliveryRetryState(
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

    public String mergePushDeliveryState(String metadataJson, PushDeliveryRetryState state) {
        ObjectNode root = readRootObject(metadataJson);
        ObjectNode pushDelivery = objectMapper.createObjectNode();
        pushDelivery.put(FAILURE_POLICY_KEY, state.failurePolicy().name());
        pushDelivery.put(RETRY_COUNT_KEY, state.retryCount());
        pushDelivery.put(MAX_RETRY_COUNT_KEY, state.maxRetryCount());
        if (state.lastError() != null) {
            pushDelivery.put(LAST_ERROR_KEY, state.lastError());
        }
        if (state.lastAttemptAt() != null) {
            pushDelivery.put(LAST_ATTEMPT_AT_KEY, state.lastAttemptAt().toString());
        }
        root.set(PUSH_DELIVERY_KEY, pushDelivery);
        return writeRoot(root);
    }

    public String clearPushDeliveryState(String metadataJson) {
        ObjectNode root = readRootObject(metadataJson);
        root.remove(PUSH_DELIVERY_KEY);
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
