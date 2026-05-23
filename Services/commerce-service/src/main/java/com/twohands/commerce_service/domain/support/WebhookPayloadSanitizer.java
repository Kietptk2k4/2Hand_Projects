package com.twohands.commerce_service.domain.support;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.LinkedHashMap;
import java.util.Map;

public final class WebhookPayloadSanitizer {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private WebhookPayloadSanitizer() {
    }

    public static Map<String, Object> sanitize(String provider, String payloadJson) {
        if (payloadJson == null || payloadJson.isBlank()) {
            return Map.of();
        }
        try {
            JsonNode root = OBJECT_MAPPER.readTree(payloadJson);
            if ("PAYOS".equalsIgnoreCase(provider)) {
                return sanitizePayos(root);
            }
            if ("GHN".equalsIgnoreCase(provider)) {
                return sanitizeGhn(root);
            }
            return Map.of();
        } catch (JsonProcessingException ex) {
            return Map.of("parse_error", true);
        }
    }

    private static Map<String, Object> sanitizePayos(JsonNode root) {
        Map<String, Object> summary = new LinkedHashMap<>();
        putText(summary, "code", root.path("code"));
        putText(summary, "desc", root.path("desc"));
        putText(summary, "success", root.path("success"));

        JsonNode data = root.path("data");
        if (!data.isMissingNode() && !data.isNull()) {
            putText(summary, "order_code", data.path("orderCode"));
            putText(summary, "payment_link_id", data.path("paymentLinkId"));
            putText(summary, "amount", data.path("amount"));
            putText(summary, "description", data.path("description"));
        }
        return summary;
    }

    private static Map<String, Object> sanitizeGhn(JsonNode root) {
        Map<String, Object> summary = new LinkedHashMap<>();
        putText(summary, "order_code", root.path("OrderCode"));
        if (root.path("OrderCode").isMissingNode()) {
            putText(summary, "order_code", root.path("order_code"));
        }
        putText(summary, "status", root.path("Status"));
        if (root.path("Status").isMissingNode()) {
            putText(summary, "status", root.path("status"));
        }
        putText(summary, "action", root.path("Action"));
        putText(summary, "type", root.path("Type"));
        return summary;
    }

    private static void putText(Map<String, Object> summary, String key, JsonNode node) {
        if (node == null || node.isMissingNode() || node.isNull()) {
            return;
        }
        if (node.isValueNode()) {
            summary.put(key, node.asText());
        }
    }
}
