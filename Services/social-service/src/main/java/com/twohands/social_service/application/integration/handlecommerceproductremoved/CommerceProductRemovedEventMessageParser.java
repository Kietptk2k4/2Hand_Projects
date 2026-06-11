package com.twohands.social_service.application.integration.handlecommerceproductremoved;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class CommerceProductRemovedEventMessageParser {

    private final ObjectMapper objectMapper;

    public CommerceProductRemovedEventMessageParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public HandleCommerceProductRemovedCommand parse(String rawMessage) {
        if (rawMessage == null || rawMessage.isBlank()) {
            throw new InvalidCommerceProductRemovedEventException("Event message body is empty");
        }

        try {
            JsonNode root = objectMapper.readTree(rawMessage);
            JsonNode payload = root.hasNonNull("payload") && root.get("payload").isObject()
                    ? root.get("payload")
                    : root;

            UUID eventId = uuid(root, "event_id");
            if (eventId == null) {
                eventId = uuid(payload, "event_id");
            }
            if (eventId == null) {
                throw new InvalidCommerceProductRemovedEventException("event_id is required in message");
            }

            String productId = text(payload, "product_id");
            if (productId == null) {
                throw new InvalidCommerceProductRemovedEventException("product_id is required in payload");
            }

            return new HandleCommerceProductRemovedCommand(eventId, productId);
        } catch (InvalidCommerceProductRemovedEventException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new InvalidCommerceProductRemovedEventException(
                    "Cannot parse commerce product removed event message: " + ex.getMessage()
            );
        }
    }

    private String text(JsonNode node, String field) {
        if (node == null || !node.hasNonNull(field)) {
            return null;
        }
        String value = node.get(field).asText();
        return value.isBlank() ? null : value;
    }

    private UUID uuid(JsonNode node, String field) {
        String value = text(node, field);
        if (value == null) {
            return null;
        }
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException ex) {
            throw new InvalidCommerceProductRemovedEventException("Invalid UUID for field " + field);
        }
    }
}
