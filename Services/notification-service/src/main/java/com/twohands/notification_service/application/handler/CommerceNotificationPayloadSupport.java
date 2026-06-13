package com.twohands.notification_service.application.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

final class CommerceNotificationPayloadSupport {

    static final String RECIPIENT_AUDIENCE_BUYER = "buyer";
    static final String RECIPIENT_AUDIENCE_SELLER = "seller";
    static final String RECIPIENT_AUDIENCE_FIELD = "recipient_audience";

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private CommerceNotificationPayloadSupport() {
    }

    static String withRecipientAudience(String rawPayload, String audience) {
        try {
            ObjectNode node;
            if (rawPayload == null || rawPayload.isBlank()) {
                node = OBJECT_MAPPER.createObjectNode();
            } else {
                JsonNode parsed = OBJECT_MAPPER.readTree(rawPayload);
                node = parsed != null && parsed.isObject()
                        ? (ObjectNode) parsed.deepCopy()
                        : OBJECT_MAPPER.createObjectNode();
            }
            node.put(RECIPIENT_AUDIENCE_FIELD, audience);
            return OBJECT_MAPPER.writeValueAsString(node);
        } catch (Exception ex) {
            return rawPayload;
        }
    }

    static List<UUID> parseSellerIds(JsonNode payload, String singleSellerId) {
        Set<UUID> sellerIds = new LinkedHashSet<>();
        addSellerId(sellerIds, singleSellerId);

        JsonNode sellerIdsNode = payload.get("seller_ids");
        if (sellerIdsNode != null && sellerIdsNode.isArray()) {
            for (JsonNode node : sellerIdsNode) {
                if (node != null && node.isTextual()) {
                    addSellerId(sellerIds, node.asText());
                }
            }
        }

        return List.copyOf(sellerIds);
    }

    private static void addSellerId(Set<UUID> sellerIds, String rawValue) {
        UUID parsed = parseUuid(rawValue);
        if (parsed != null) {
            sellerIds.add(parsed);
        }
    }

    private static UUID parseUuid(String rawValue) {
        if (rawValue == null || rawValue.isBlank()) {
            return null;
        }
        try {
            return UUID.fromString(rawValue.trim());
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
}
