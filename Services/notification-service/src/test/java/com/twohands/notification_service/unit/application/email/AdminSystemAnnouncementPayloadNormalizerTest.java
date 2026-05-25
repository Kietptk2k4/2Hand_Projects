package com.twohands.notification_service.unit.application.email;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.twohands.notification_service.application.email.AdminSystemAnnouncementPayloadNormalizer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AdminSystemAnnouncementPayloadNormalizerTest {

    private AdminSystemAnnouncementPayloadNormalizer normalizer;

    @BeforeEach
    void setUp() {
        normalizer = new AdminSystemAnnouncementPayloadNormalizer(new ObjectMapper());
    }

    @Test
    void normalizeForStorage_mapsPinnedAliasToIsPinned() {
        String normalized = normalizer.normalizeForStorage(
                "SYSTEM_ANNOUNCEMENT_SENT",
                """
                        {
                          "announcement_id":"ann-1",
                          "title":"T",
                          "content":"C",
                          "severity":"INFO",
                          "pinned":true
                        }
                        """
        );

        assertTrue(normalized.contains("\"is_pinned\":true"));
        assertFalse(normalized.contains("\"pinned\""));
    }

    @Test
    void normalizeForStorage_mapsIdAndStripsInternalFields() {
        UUID announcementId = UUID.randomUUID();
        UUID createdBy = UUID.randomUUID();

        String normalized = normalizer.normalizeForStorage(
                "SYSTEM_ANNOUNCEMENT_SENT",
                """
                        {
                          "id":"%s",
                          "title":"Hello",
                          "content":"Body",
                          "severity":"INFO",
                          "is_pinned":false,
                          "dismissible":true,
                          "status":"SENT",
                          "created_by":"%s"
                        }
                        """.formatted(announcementId, createdBy)
        );

        assertTrue(normalized.contains("\"announcement_id\":\"" + announcementId + "\""));
        assertFalse(normalized.contains("created_by"));
        assertFalse(normalized.contains("\"status\""));
    }
}
