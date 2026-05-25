package com.twohands.notification_service.domain.email;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

public final class EmailNotificationTemplatePolicy {

    private static final Map<String, EmailNotificationTemplate> TEMPLATES = Map.ofEntries(
            Map.entry(
                    "EMAIL_VERIFICATION_REQUESTED",
                    new EmailNotificationTemplate(
                            "Verify your 2Hands email",
                            """
                                    Hello,

                                    Please verify your email using this link:
                                    {{verification_link}}

                                    If you did not request this, you can ignore this message.
                                    """,
                            Set.of("recipient_email", "verification_link")
                    )
            ),
            Map.entry(
                    "PASSWORD_RESET_REQUESTED",
                    new EmailNotificationTemplate(
                            "Reset your 2Hands password",
                            """
                                    Hello,

                                    Use this link to reset your password:
                                    {{reset_link}}

                                    If you did not request a password reset, you can ignore this message.
                                    """,
                            Set.of("recipient_email", "reset_link")
                    )
            ),
            Map.entry(
                    "PASSWORD_CHANGED",
                    new EmailNotificationTemplate(
                            "Your 2Hands password was changed",
                            """
                                    Hello{{recipient_name}},

                                    Your account password was changed recently.
                                    If this was not you, contact support immediately.
                                    """,
                            Set.of("recipient_email")
                    )
            ),
            Map.entry(
                    "ORDER_CREATED",
                    new EmailNotificationTemplate(
                            "Order {{order_code}} confirmed",
                            """
                                    Hello{{recipient_name}},

                                    Your order {{order_code}} has been created successfully.{{order_summary_line}}
                                    We will notify you when it progresses.
                                    """,
                            Set.of("recipient_email", "order_code")
                    )
            ),
            Map.entry(
                    "PAYMENT_SUCCESS",
                    new EmailNotificationTemplate(
                            "Payment received for order {{order_code}}",
                            """
                                    Hello{{recipient_name}},

                                    We received your payment for order {{order_code}}.
                                    Thank you for shopping with 2Hands.
                                    """,
                            Set.of("recipient_email", "order_code")
                    )
            ),
            Map.entry(
                    "USER_SUSPENDED",
                    new EmailNotificationTemplate(
                            "Your 2Hands account has been suspended",
                            """
                                    Hello,

                                    Your account has been suspended due to a policy enforcement action.
                                    {{enforcement_reason_line}}{{enforcement_expires_at_line}}
                                    Contact support if you need assistance.
                                    """,
                            Set.of("recipient_email")
                    )
            ),
            Map.entry(
                    "USER_RESTRICTED",
                    new EmailNotificationTemplate(
                            "Your 2Hands account has been restricted",
                            """
                                    Hello,

                                    Your account access has been restricted due to a policy enforcement action.
                                    {{enforcement_reason_line}}{{enforcement_expires_at_line}}
                                    Contact support if you need assistance.
                                    """,
                            Set.of("recipient_email")
                    )
            ),
            Map.entry(
                    "SHOP_SUSPENDED",
                    new EmailNotificationTemplate(
                            "Your 2Hands shop has been suspended",
                            """
                                    Hello,

                                    Your shop has been suspended due to a policy enforcement action.
                                    Contact support if you need assistance.
                                    """,
                            Set.of("recipient_email")
                    )
            ),
            Map.entry(
                    "USER_CREATED",
                    new EmailNotificationTemplate(
                            "Welcome to 2Hands",
                            """
                                    Hello{{recipient_name}},

                                    Welcome to 2Hands. Your account has been created successfully.
                                    """,
                            Set.of("recipient_email")
                    )
            )
    );

    private EmailNotificationTemplatePolicy() {
    }

    public static Optional<EmailNotificationTemplate> resolve(String eventType) {
        if (eventType == null || eventType.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(TEMPLATES.get(eventType));
    }
}
