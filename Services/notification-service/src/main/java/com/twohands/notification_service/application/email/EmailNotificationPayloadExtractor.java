package com.twohands.notification_service.application.email;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@Component
public class EmailNotificationPayloadExtractor {

    private final ObjectMapper objectMapper;

    public EmailNotificationPayloadExtractor(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public Map<String, String> extract(String payloadJson) {
        Map<String, String> values = new HashMap<>();
        if (payloadJson == null || payloadJson.isBlank()) {
            return values;
        }

        try {
            JsonNode root = unwrapTextualJson(objectMapper.readTree(payloadJson));
            if (root.isObject()) {
                collectObjectValues(root, values);
            }
        } catch (Exception ignored) {
            // Missing variables are validated later.
        }
        return values;
    }

    private JsonNode unwrapTextualJson(JsonNode node) {
        if (node == null || !node.isTextual()) {
            return node;
        }
        String text = node.asText();
        if (text == null || text.isBlank()) {
            return objectMapper.createObjectNode();
        }
        try {
            return unwrapTextualJson(objectMapper.readTree(text));
        } catch (Exception ex) {
            return node;
        }
    }

    private void collectObjectValues(JsonNode node, Map<String, String> values) {
        Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> field = fields.next();
            JsonNode valueNode = field.getValue();
            if (valueNode == null || valueNode.isNull()) {
                continue;
            }
            if (valueNode.isValueNode()) {
                values.put(field.getKey(), valueNode.asText());
            }
        }
    }
}
