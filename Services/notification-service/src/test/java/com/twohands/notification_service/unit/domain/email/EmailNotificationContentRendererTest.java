package com.twohands.notification_service.unit.domain.email;

import com.twohands.notification_service.domain.email.EmailNotificationContentRenderer;
import com.twohands.notification_service.domain.email.EmailNotificationTemplate;
import com.twohands.notification_service.domain.email.EmailNotificationTemplatePolicy;
import com.twohands.notification_service.domain.email.EmailNotificationVariablesPolicy;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EmailNotificationContentRendererTest {

    @Test
    void render_replacesTemplateVariables() {
        var template = new EmailNotificationTemplate(
                "Order {{order_code}} confirmed",
                "Hello{{recipient_name}}, your order {{order_code}} is confirmed.",
                Set.of("recipient_email", "order_code")
        );

        var content = EmailNotificationContentRenderer.render(template, Map.of(
                "recipient_email", "buyer@example.com",
                "recipient_name", " Buyer",
                "order_code", "ORD-1001"
        ));

        assertEquals("buyer@example.com", content.to());
        assertEquals("Order ORD-1001 confirmed", content.subject());
        assertEquals("Hello Buyer, your order ORD-1001 is confirmed.", content.body());
        assertFalse(content.body().contains("{{"));
    }

    @Test
    void render_emailVerificationTemplateUsesOtpCodeNotLink() {
        var template = EmailNotificationTemplatePolicy.resolve("EMAIL_VERIFICATION_REQUESTED").orElseThrow();
        var variables = EmailNotificationVariablesPolicy.extract(Map.of(
                "recipient_email", "user@example.com",
                "verification_code", "123456"
        ));
        EmailNotificationVariablesPolicy.validateRequired(template, variables);

        var content = EmailNotificationContentRenderer.render(template, variables);

        assertEquals("Verify your 2Hands email", content.subject());
        assertTrue(content.body().contains("Mã xác thực của bạn: 123456"));
        assertFalse(content.body().contains("verification_link"));
        assertFalse(content.body().contains("http"));
    }
}
