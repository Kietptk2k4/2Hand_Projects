package com.twohands.notification_service.unit.application.email;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.twohands.notification_service.application.email.AdminPostModerationPayloadNormalizer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AdminPostModerationPayloadNormalizerTest {

    private AdminPostModerationPayloadNormalizer normalizer;

    @BeforeEach
    void setUp() {
        normalizer = new AdminPostModerationPayloadNormalizer(new ObjectMapper());
    }

    @Test
    void normalizeForStorage_mapsAdminPostModeratedPayloadToUserSafeFields() {
        String result = normalizer.normalizeForStorage(
                "POST_MODERATED",
                """
                        {
                          "post_id": "507f1f77bcf86cd799439011",
                          "author_user_id": "11111111-1111-1111-1111-111111111111",
                          "action": "HIDE",
                          "reason": "Spam content",
                          "moderated_by": "22222222-2222-2222-2222-222222222222",
                          "moderation_log_id": "33333333-3333-3333-3333-333333333333"
                        }
                        """
        );

        assertTrue(result.contains("\"author_user_id\":\"11111111-1111-1111-1111-111111111111\""));
        assertTrue(result.contains("\"moderation_reason\":\"Spam content\""));
        assertFalse(result.contains("moderated_by"));
        assertFalse(result.contains("moderation_log_id"));
        assertFalse(result.contains("\"reason\""));
    }
}
