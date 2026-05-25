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
public class AuthSecurityEmailNotificationPayloadNormalizer {

    private static final String EMAIL_VERIFICATION_REQUESTED = "EMAIL_VERIFICATION_REQUESTED";
    private static final String PASSWORD_RESET_REQUESTED = "PASSWORD_RESET_REQUESTED";

    private final ObjectMapper objectMapper;
    private final NotificationEmailProperties notificationEmailProperties;

    public AuthSecurityEmailNotificationPayloadNormalizer(
            ObjectMapper objectMapper,
            NotificationEmailProperties notificationEmailProperties
    ) {
        this.objectMapper = objectMapper;
        this.notificationEmailProperties = notificationEmailProperties;
    }

    public String normalizeForStorage(String eventType, String rawPayload) {
        if (rawPayload == null || rawPayload.isBlank()) {
            return rawPayload;
        }
        if (EMAIL_VERIFICATION_REQUESTED.equals(eventType)) {
            return normalizeEmailVerification(rawPayload);
        }
        if (PASSWORD_RESET_REQUESTED.equals(eventType)) {
            return normalizePasswordReset(rawPayload);
        }
        return rawPayload;
    }

    private String normalizeEmailVerification(String rawPayload) {
        try {
            ObjectNode normalized = parseObjectPayload(rawPayload);
            if (normalized == null) {
                return rawPayload;
            }
            normalizeRecipientEmail(normalized);
            normalizeVerificationDelivery(normalized);
            normalized.remove("verification_token");
            normalized.remove("verification_token_type");
            return objectMapper.writeValueAsString(normalized);
        } catch (JsonProcessingException ex) {
            return rawPayload;
        }
    }

    private String normalizePasswordReset(String rawPayload) {
        try {
            ObjectNode normalized = parseObjectPayload(rawPayload);
            if (normalized == null) {
                return rawPayload;
            }
            normalizeRecipientEmail(normalized);
            normalizePasswordResetDelivery(normalized);
            normalized.remove("verification_token");
            normalized.remove("verification_token_type");
            normalized.remove("reset_token");
            return objectMapper.writeValueAsString(normalized);
        } catch (JsonProcessingException ex) {
            return rawPayload;
        }
    }

    private ObjectNode parseObjectPayload(String rawPayload) throws JsonProcessingException {
        JsonNode root = objectMapper.readTree(rawPayload);
        if (!root.isObject()) {
            return null;
        }
        return ((ObjectNode) root).deepCopy();
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

        String token = firstNonBlank(
                textValue(payload, "verification_token"),
                textValue(payload, "verify_token")
        );
        if (token == null) {
            return;
        }

        String link = buildLink(notificationEmailProperties.verificationLinkBaseUrl(), token);
        if (link != null) {
            payload.put("verification_link", link);
        } else {
            payload.put("verification_code", token);
        }
    }

    private void normalizePasswordResetDelivery(ObjectNode payload) {
        if (hasText(payload, "reset_link") || hasText(payload, "reset_code")) {
            return;
        }

        String token = firstNonBlank(
                textValue(payload, "reset_token"),
                textValue(payload, "password_reset_token"),
                textValue(payload, "verification_token")
        );
        if (token == null) {
            return;
        }

        String link = buildLink(notificationEmailProperties.passwordResetLinkBaseUrl(), token);
        if (link != null) {
            payload.put("reset_link", link);
        } else {
            payload.put("reset_code", token);
        }
    }

    private String buildLink(String baseUrl, String token) {
        if (baseUrl == null || baseUrl.isBlank()) {
            return null;
        }
        String trimmedBase = baseUrl.trim();
        String encodedToken = URLEncoder.encode(token, StandardCharsets.UTF_8);
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

    private static String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }
}
