package com.twohands.notification_service.unit.domain.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.twohands.notification_service.domain.admin.SystemAnnouncementNotificationMetadataPolicy;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SystemAnnouncementNotificationMetadataPolicyTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void build_storesPinnedFlagAndNormalizedSeverity() {
        String metadata = SystemAnnouncementNotificationMetadataPolicy.build(
                objectMapper,
                "ann-1",
                "critical",
                true,
                true
        );

        assertTrue(metadata.contains("\"is_pinned\":true"));
        assertTrue(metadata.contains("\"dismissible\":true"));
        assertTrue(metadata.contains("\"severity\":\"CRITICAL\""));
        assertTrue(metadata.contains("\"announcement_id\":\"ann-1\""));
    }

    @Test
    void build_storesNonPinnedMetadata() {
        String metadata = SystemAnnouncementNotificationMetadataPolicy.build(
                objectMapper,
                "ann-2",
                "INFO",
                false,
                false
        );

        assertTrue(metadata.contains("\"is_pinned\":false"));
    }

    @Test
    void normalizeSeverity_rejectsUnknownValue() {
        assertThrows(
                IllegalArgumentException.class,
                () -> SystemAnnouncementNotificationMetadataPolicy.normalizeSeverity("LOW")
        );
    }

    @Test
    void sanitizeForResponse_defaultsMissingPinToFalse() {
        String sanitized = SystemAnnouncementNotificationMetadataPolicy.sanitizeForResponse(
                objectMapper,
                "{\"severity\":\"INFO\"}"
        );

        assertTrue(sanitized.contains("\"is_pinned\":false"));
    }

    @Test
    void sanitizeForResponse_coercesInvalidPinToFalse() {
        String sanitized = SystemAnnouncementNotificationMetadataPolicy.sanitizeForResponse(
                objectMapper,
                "{\"is_pinned\":\"yes\",\"severity\":\"INFO\"}"
        );

        assertFalse(sanitized.contains("\"yes\""));
        assertTrue(sanitized.contains("\"is_pinned\":false"));
    }
}
