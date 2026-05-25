package com.twohands.notification_service.unit.domain.email;

import com.twohands.notification_service.domain.email.EmailNotificationContentRenderer;
import com.twohands.notification_service.domain.email.EmailNotificationTemplate;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

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
}
