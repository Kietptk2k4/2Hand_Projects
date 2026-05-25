package com.twohands.notification_service.domain.admin;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

public final class AccountEnforcementRestrictedCapabilitiesPolicy {

    private static final int MAX_CAPABILITY_LENGTH = 80;
    private static final int MAX_SUMMARY_CAPABILITIES = 5;
    private static final Pattern BLOCKED_TERM = Pattern.compile(
            "(?i)\\b(internal|confidential|admin[_ -]?only|investigation)\\b"
    );

    private static final Map<String, String> KNOWN_CAPABILITY_LABELS = Map.ofEntries(
            Map.entry("POST_CREATE", "Creating posts"),
            Map.entry("COMMENT_CREATE", "Commenting"),
            Map.entry("REVIEW_CREATE", "Writing reviews"),
            Map.entry("PRODUCT_CREATE", "Listing products"),
            Map.entry("MESSAGE_SEND", "Sending messages"),
            Map.entry("SHOP_WRITE", "Managing shop content")
    );

    private AccountEnforcementRestrictedCapabilitiesPolicy() {
    }

    public static String resolveSummary(JsonNode capabilitiesNode) {
        if (capabilitiesNode == null || capabilitiesNode.isNull()) {
            return null;
        }

        List<String> rawValues = new ArrayList<>();
        if (capabilitiesNode.isArray()) {
            for (JsonNode node : capabilitiesNode) {
                if (node != null && node.isValueNode()) {
                    String value = node.asText();
                    if (value != null && !value.isBlank()) {
                        rawValues.add(value.trim());
                    }
                }
            }
        } else if (capabilitiesNode.isValueNode()) {
            String value = capabilitiesNode.asText();
            if (value != null && !value.isBlank()) {
                for (String part : value.split(",")) {
                    if (!part.isBlank()) {
                        rawValues.add(part.trim());
                    }
                }
            }
        }

        if (rawValues.isEmpty()) {
            return null;
        }

        Set<String> labels = new LinkedHashSet<>();
        for (String raw : rawValues) {
            String label = toUserFacingLabel(raw);
            if (label != null && !label.isBlank()) {
                labels.add(label);
            }
            if (labels.size() >= MAX_SUMMARY_CAPABILITIES) {
                break;
            }
        }

        if (labels.isEmpty()) {
            return null;
        }
        return String.join(", ", labels);
    }

    private static String toUserFacingLabel(String raw) {
        String sanitized = sanitize(raw);
        if (sanitized == null) {
            return null;
        }

        String known = KNOWN_CAPABILITY_LABELS.get(sanitized.toUpperCase(Locale.ROOT));
        if (known != null) {
            return known;
        }

        String normalized = sanitized.toLowerCase(Locale.ROOT).replace('_', ' ').trim();
        if (normalized.isBlank() || BLOCKED_TERM.matcher(normalized).find()) {
            return null;
        }
        return Character.toUpperCase(normalized.charAt(0)) + normalized.substring(1);
    }

    private static String sanitize(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String trimmed = value.trim().replace("<", "").replace(">", "");
        if (trimmed.length() > MAX_CAPABILITY_LENGTH) {
            return trimmed.substring(0, MAX_CAPABILITY_LENGTH);
        }
        return trimmed;
    }
}
