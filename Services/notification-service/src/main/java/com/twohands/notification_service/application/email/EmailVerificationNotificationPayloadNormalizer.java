package com.twohands.notification_service.application.email;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.twohands.notification_service.config.NotificationEmailProperties;
import org.springframework.stereotype.Component;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
public class EmailVerificationNotificationPayloadNormalizer {

    private static final String EVENT_TYPE = "EMAIL_VERIFICATION_REQUESTED";

    private final ObjectMapper objectMapper;
    private final NotificationEmailProperties notificationEmailProperties;

    public EmailVerificationNotificationPayloadNormalizer(
            ObjectMapper objectMapper,
            NotificationEmailProperties notificationEmailProperties
    ) {
        this.objectMapper = objectMapper;
        this.notificationEmailProperties = notificationEmailProperties;
    }

    public String normalizeForStorage(String eventType, String rawPayload) {
        if (!EVENT_TYPE.equals(eventType) || rawPayload == null || rawPayload.isBlank()) {
            return rawPayload;
        }

        try {
            JsonNode root = objectMapper.readTree(rawPayload);
            if (!root.isObject()) {
                return rawPayload;
            }
            ObjectNode normalized = ((ObjectNode) root).deepCopy();
            normalizeRecipientEmail(normalized);
            normalizeVerificationDelivery(normalized);
            normalized.remove("verification_token");
            normalized.remove("verification_token_type");
            return objectMapper.writeValueAsString(normalized);
        } catch (JsonProcessingException ex) {
            return rawPayload;
        }
    }

    private void normalizeRecipientEmail(ObjectNode payload) {
        if (hasText(payload, "recipient_email")) {
            return;
        }
        copyTextField(payload, "email", "recipient_email");
    }

    private void normalizeVerificationDelivery(ObjectNode payload) {
        if (hasText(payload, "verification_link") || hasText(payload, "verification_code")) {
            return;
        }

        String verificationToken = textValue(payload, "verification_token");
        if (verificationToken == null) {
            return;
        }

        String verificationLink = buildVerificationLink(verificationToken);
        if (verificationLink != null) {
            payload.put("verification_link", verificationLink);
        } else {
            payload.put("verification_code", verificationToken);
        }
    }

    private String buildVerificationLink(String verificationToken) {
        String baseUrl = notificationEmailProperties.verificationLinkBaseUrl();
        if (baseUrl == null || baseUrl.isBlank()) {
            return null;
        }
        String trimmedBase = baseUrl.trim();
        String encodedToken = URLEncoder.encode(verificationToken, StandardCharsets.UTF_8);
        if (trimmedBase.contains("{{token}}")) {
            return trimmedBase.replace("{{token}}", encodedToken);
        }
        String separator = trimmedBase.contains("?") ? "&" : "?";
        return trimmedBase + separator + "token=" + encodedToken;
    }

    private static void copyTextField(ObjectNode payload, String sourceField, String targetField) {
        String value = textValue(payload, sourceField);
        if (value != null) {
            payload.put(targetField, value);
        }
    }

    private static boolean hasText(ObjectNode payload, String field) {
        String value = textValue(payload, field);
        return value != null && !value.isBlank();
    }

    private static String textValue(ObjectNode payload, String field) {
        JsonNode node = payload.get(field);
        if (node == null || node.isNull() || !node.isValueNode()) {
            return null;
        }
        String value = node.asText();
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
