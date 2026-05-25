package com.twohands.notification_service.unit.application.email;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.twohands.notification_service.application.email.EmailVerificationNotificationPayloadNormalizer;
import com.twohands.notification_service.config.NotificationEmailProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EmailVerificationNotificationPayloadNormalizerTest {

    private EmailVerificationNotificationPayloadNormalizer normalizer;

    @BeforeEach
    void setUp() {
        NotificationEmailProperties properties = new NotificationEmailProperties();
        properties.setVerificationLinkBaseUrl("https://2hands.vn/verify-email");
        normalizer = new EmailVerificationNotificationPayloadNormalizer(new ObjectMapper(), properties);
    }

    @Test
    void normalizeForStorage_mapsAuthPayloadToDeliverableFieldsWithoutRawToken() {
        String result = normalizer.normalizeForStorage(
                "EMAIL_VERIFICATION_REQUESTED",
                """
                        {
                          "user_id": "11111111-1111-1111-1111-111111111111",
                          "email": "user@example.com",
                          "verification_token": "raw-secret-token",
                          "verification_token_type": "EMAIL_VERIFY"
                        }
                        """
        );

        assertTrue(result.contains("\"recipient_email\":\"user@example.com\""));
        assertTrue(result.contains("verification_link"));
        assertTrue(result.contains("token=raw-secret-token"));
        assertFalse(result.contains("verification_token"));
    }

    @Test
    void normalizeForStorage_preservesExplicitVerificationCode() {
        String result = normalizer.normalizeForStorage(
                "EMAIL_VERIFICATION_REQUESTED",
                """
                        {
                          "recipient_email": "user@example.com",
                          "verification_code": "123456"
                        }
                        """
        );

        assertTrue(result.contains("\"verification_code\":\"123456\""));
        assertFalse(result.contains("verification_link"));
    }

    @Test
    void normalizeForStorage_leavesNonVerificationEventsUntouched() {
        String payload = "{\"email\":\"user@example.com\",\"verification_token\":\"secret\"}";

        assertEquals(payload, normalizer.normalizeForStorage("POST_LIKED", payload));
    }
}
