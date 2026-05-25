package com.twohands.notification_service.unit.application.email;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.twohands.notification_service.application.email.AdminReviewModerationPayloadNormalizer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AdminReviewModerationPayloadNormalizerTest {

    private AdminReviewModerationPayloadNormalizer normalizer;

    @BeforeEach
    void setUp() {
        normalizer = new AdminReviewModerationPayloadNormalizer(new ObjectMapper());
    }

    @Test
    void normalizeForStorage_mapsAuthorAndUserSafeReason() {
        String result = normalizer.normalizeForStorage(
                "REVIEW_HIDDEN",
                """
                        {
                          "review_id": "review-1",
                          "author_id": "11111111-1111-1111-1111-111111111111",
                          "reason": "Inappropriate content",
                          "hidden_by": "admin-uuid",
                          "note": "internal admin note"
                        }
                        """
        );

        assertTrue(result.contains("\"review_author_id\":\"11111111-1111-1111-1111-111111111111\""));
        assertTrue(result.contains("\"hidden_reason\":\"Inappropriate content\""));
        assertFalse(result.contains("hidden_by"));
        assertFalse(result.contains("internal admin note"));
        assertFalse(result.contains("\"reason\""));
    }
}
