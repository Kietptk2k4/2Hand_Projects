package com.twohands.notification_service.unit.domain.email;

import com.twohands.notification_service.domain.email.EmailNotificationContentRenderer;
import com.twohands.notification_service.domain.email.EmailNotificationTemplate;
import com.twohands.notification_service.domain.email.EmailNotificationVariablesPolicy;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EmailNotificationVariablesPolicyTest {

    @Test
    void extract_mapsRecipientEmailAndVerificationLink() {
        var variables = EmailNotificationVariablesPolicy.extract(Map.of(
                "email", "user@example.com",
                "verify_link", "https://2hands.vn/verify?token=secret"
        ));

        assertEquals("user@example.com", variables.get("recipient_email"));
        assertEquals("https://2hands.vn/verify?token=secret", variables.get("verification_link"));
    }

    @Test
    void extract_keepsVerificationCodeWithoutSynthesizingLink() {
        var variables = EmailNotificationVariablesPolicy.extract(Map.of(
                "recipient_email", "user@example.com",
                "verification_code", "123456"
        ));

        assertEquals("123456", variables.get("verification_code"));
        assertFalse(variables.containsKey("verification_link"));
    }

    @Test
    void validateRequired_rejectsInvalidEmail() {
        var template = new EmailNotificationTemplate("Subject", "Body", Set.of("recipient_email"));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                EmailNotificationVariablesPolicy.validateRequired(template, Map.of("recipient_email", "invalid"))
        );

        assertEquals("Invalid recipient email.", ex.getMessage());
    }

    @Test
    void extract_usesResetCodeWhenLinkMissing() {
        var variables = EmailNotificationVariablesPolicy.extract(Map.of(
                "recipient_email", "user@example.com",
                "reset_code", "654321"
        ));

        assertEquals("654321", variables.get("reset_link"));
    }

    @Test
    void extract_buildsPaymentSummaryLineForPaymentSuccessEmail() {
        var variables = EmailNotificationVariablesPolicy.extract(Map.of(
                "recipient_email", "buyer@example.com",
                "order_code", "ORD-100",
                "amount", "100000",
                "payment_method", "COD"
        ));

        assertTrue(variables.get("payment_summary_line").contains("Amount paid: 100000"));
        assertTrue(variables.get("payment_summary_line").contains("Payment method: COD"));
    }

    @Test
    void maskEmail_hidesLocalPart() {
        assertEquals("u***@example.com", EmailNotificationVariablesPolicy.maskEmail("user@example.com"));
    }
}
