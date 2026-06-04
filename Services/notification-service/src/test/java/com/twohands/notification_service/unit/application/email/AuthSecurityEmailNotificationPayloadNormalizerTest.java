package com.twohands.notification_service.unit.application.email;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.twohands.notification_service.application.email.AuthSecurityEmailNotificationPayloadNormalizer;
import com.twohands.notification_service.config.NotificationEmailProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuthSecurityEmailNotificationPayloadNormalizerTest {

    private AuthSecurityEmailNotificationPayloadNormalizer normalizer;

    @BeforeEach
    void setUp() {
        NotificationEmailProperties properties = new NotificationEmailProperties();
        properties.setVerificationLinkBaseUrl("https://2hands.vn/verify-email");
        properties.setPasswordResetLinkBaseUrl("https://2hands.vn/reset-password");
        normalizer = new AuthSecurityEmailNotificationPayloadNormalizer(new ObjectMapper(), properties);
    }

    @Test
    void normalizeForStorage_mapsAuthVerificationPayloadToOtpCodeWithoutLink() {
        String result = normalizer.normalizeForStorage(
                "EMAIL_VERIFICATION_REQUESTED",
                """
                        {
                          "user_id": "11111111-1111-1111-1111-111111111111",
                          "email": "user@example.com",
                          "verification_code": "123456",
                          "verification_token": "123456",
                          "verification_token_type": "EMAIL_VERIFY"
                        }
                        """
        );

        assertTrue(result.contains("\"recipient_email\":\"user@example.com\""));
        assertTrue(result.contains("\"verification_code\":\"123456\""));
        assertFalse(result.contains("verification_link"));
        assertFalse(result.contains("\"verification_token\""));
    }

    @Test
    void normalizeForStorage_mapsLegacyVerificationTokenFieldToCode() {
        String result = normalizer.normalizeForStorage(
                "EMAIL_VERIFICATION_REQUESTED",
                """
                        {
                          "email": "user@example.com",
                          "verification_token": "654321"
                        }
                        """
        );

        assertTrue(result.contains("\"verification_code\":\"654321\""));
        assertFalse(result.contains("verification_link"));
        assertFalse(result.contains("\"verification_token\""));
    }

    @Test
    void normalizeForStorage_stripsVerificationLinkFromEmailVerificationPayload() {
        String result = normalizer.normalizeForStorage(
                "EMAIL_VERIFICATION_REQUESTED",
                """
                        {
                          "recipient_email": "user@example.com",
                          "verification_link": "https://2hands.vn/verify?token=abc",
                          "verification_code": "123456"
                        }
                        """
        );

        assertTrue(result.contains("\"verification_code\":\"123456\""));
        assertFalse(result.contains("verification_link"));
    }

    @Test
    void normalizeForStorage_mapsAuthPasswordResetPayloadWithoutRawTokenField() {
        String result = normalizer.normalizeForStorage(
                "PASSWORD_RESET_REQUESTED",
                """
                        {
                          "user_id": "22222222-2222-2222-2222-222222222222",
                          "email": "user@example.com",
                          "verification_token": "raw-reset-token",
                          "verification_token_type": "PASSWORD_RESET"
                        }
                        """
        );

        assertTrue(result.contains("\"recipient_email\":\"user@example.com\""));
        assertTrue(result.contains("reset_link"));
        assertTrue(result.contains("https://2hands.vn/reset-password"));
        assertTrue(result.contains("token=raw-reset-token"));
        assertFalse(result.contains("\"verification_token\""));
    }

    @Test
    void normalizeForStorage_preservesExplicitResetCode() {
        String result = normalizer.normalizeForStorage(
                "PASSWORD_RESET_REQUESTED",
                """
                        {
                          "recipient_email": "user@example.com",
                          "reset_code": "654321"
                        }
                        """
        );

        assertTrue(result.contains("\"reset_code\":\"654321\""));
        assertFalse(result.contains("reset_link"));
    }

    @Test
    void normalizeForStorage_leavesNonAuthSecurityEventsUntouched() {
        String payload = "{\"email\":\"user@example.com\",\"verification_token\":\"secret\"}";

        assertEquals(payload, normalizer.normalizeForStorage("POST_LIKED", payload));
    }
}
