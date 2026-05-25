package com.twohands.notification_service.application.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.twohands.notification_service.domain.admin.UserRestrictedNotificationContext;
import com.twohands.notification_service.domain.email.AccountEnforcementEmailReasonPolicy;
import com.twohands.notification_service.domain.notificationevent.NotificationEvent;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class UserRestrictedNotificationPayloadParser {

    private static final String USER_ENFORCEMENT_AGGREGATE_TYPE = "USER_ENFORCEMENT";
    private static final String REFERENCE_TYPE = "USER_ENFORCEMENT";

    private final ObjectMapper objectMapper;

    public UserRestrictedNotificationPayloadParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public UserRestrictedNotificationContext parse(NotificationEvent event) {
        JsonNode payload = parsePayload(event.payload());

        UUID targetUserId = firstUuid(
                event.recipientUserId(),
                textField(payload, "target_user_id"),
                textField(payload, "user_id")
        );
        if (targetUserId == null) {
            throw new IllegalArgumentException("target_user_id is required for USER_RESTRICTED notification event.");
        }

        String enforcementId = firstNonBlank(
                textField(payload, "enforcement_id"),
                USER_ENFORCEMENT_AGGREGATE_TYPE.equalsIgnoreCase(event.aggregateType()) ? event.aggregateId() : null
        );
        if (enforcementId == null || enforcementId.isBlank()) {
            throw new IllegalArgumentException("enforcement_id is required for USER_RESTRICTED notification event.");
        }

        String enforcementReason = firstNonBlank(
                textField(payload, "enforcement_reason"),
                AccountEnforcementEmailReasonPolicy.resolveUserFacingReason(
                        firstNonBlank(
                                textField(payload, "description"),
                                textField(payload, "user_reason"),
                                textField(payload, "reason")
                        ),
                        textField(payload, "reason_code")
                )
        );

        String enforcementExpiresAt = firstNonBlank(
                textField(payload, "enforcement_expires_at"),
                textField(payload, "expires_at")
        );

        String restrictedCapabilitiesSummary = firstNonBlank(
                textField(payload, "restricted_capabilities_summary"),
                textField(payload, "capability_restriction_summary")
        );

        return new UserRestrictedNotificationContext(
                targetUserId,
                enforcementId.trim(),
                enforcementReason,
                enforcementExpiresAt,
                restrictedCapabilitiesSummary,
                REFERENCE_TYPE,
                enforcementId.trim()
        );
    }

    private JsonNode parsePayload(String rawPayload) {
        if (rawPayload == null || rawPayload.isBlank()) {
            return objectMapper.createObjectNode();
        }
        try {
            JsonNode node = objectMapper.readTree(rawPayload);
            return node == null || node.isNull() ? objectMapper.createObjectNode() : node;
        } catch (Exception ex) {
            throw new IllegalArgumentException("USER_RESTRICTED event payload must be valid JSON.");
        }
    }

    private UUID firstUuid(UUID primary, String firstFallback, String secondFallback) {
        if (primary != null) {
            return primary;
        }
        UUID first = parseUuid(firstFallback);
        if (first != null) {
            return first;
        }
        return parseUuid(secondFallback);
    }

    private UUID parseUuid(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return UUID.fromString(value.trim());
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private String textField(JsonNode payload, String field) {
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

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }
}
