package com.twohands.notification_service.domain.email;

import com.twohands.notification_service.domain.commerce.OrderConfirmationEmailSummaryPolicy;
import com.twohands.notification_service.domain.commerce.PaymentSuccessEmailSummaryPolicy;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public final class EmailNotificationVariablesPolicy {

    private static final int MAX_VARIABLE_LENGTH = 500;
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\{\\{([a-zA-Z0-9_]+)}}");

    private EmailNotificationVariablesPolicy() {
    }

    public static Map<String, String> extract(Map<String, String> rawValues) {
        Map<String, String> variables = new HashMap<>();

        putEmailVariable(variables, "recipient_email", firstNonBlank(
                rawValues.get("recipient_email"),
                rawValues.get("email"),
                rawValues.get("buyer_email"),
                rawValues.get("to_email")
        ));

        putTextVariable(variables, "verification_link", firstNonBlank(
                rawValues.get("verification_link"),
                rawValues.get("verify_link")
        ));
        putTextVariable(variables, "verification_code", firstNonBlank(
                rawValues.get("verification_code"),
                rawValues.get("verification_token")
        ));

        if (!variables.containsKey("verification_link") && variables.containsKey("verification_code")) {
            variables.put("verification_link", variables.get("verification_code"));
        }

        putTextVariable(variables, "reset_link", firstNonBlank(
                rawValues.get("reset_link"),
                rawValues.get("password_reset_link")
        ));
        putTextVariable(variables, "reset_code", firstNonBlank(
                rawValues.get("reset_code"),
                rawValues.get("reset_token")
        ));

        if (!variables.containsKey("reset_link") && variables.containsKey("reset_code")) {
            variables.put("reset_link", variables.get("reset_code"));
        }

        putTextVariable(variables, "order_code", firstNonBlank(
                rawValues.get("order_code"),
                rawValues.get("order_id")
        ));
        putTextVariable(variables, "order_summary_line", OrderConfirmationEmailSummaryPolicy.formatAmountLine(
                firstNonBlank(
                        rawValues.get("final_amount"),
                        rawValues.get("total_amount")
                )
        ));
        putTextVariable(variables, "payment_summary_line", PaymentSuccessEmailSummaryPolicy.formatSummaryLine(
                firstNonBlank(
                        rawValues.get("amount"),
                        rawValues.get("final_amount"),
                        rawValues.get("total_amount")
                ),
                rawValues.get("payment_method")
        ));

        putOptionalNameVariable(variables, "recipient_name", firstNonBlank(
                rawValues.get("recipient_name"),
                rawValues.get("user_name"),
                rawValues.get("buyer_name")
        ));

        String enforcementReason = AccountEnforcementEmailReasonPolicy.resolveUserFacingReason(
                firstNonBlank(
                        rawValues.get("enforcement_reason"),
                        rawValues.get("description"),
                        rawValues.get("user_reason"),
                        rawValues.get("reason")
                ),
                rawValues.get("reason_code")
        );
        variables.put(
                "enforcement_reason_line",
                AccountEnforcementEmailReasonPolicy.formatReasonLine(enforcementReason)
        );
        variables.put(
                "enforcement_expires_at_line",
                AccountEnforcementEmailReasonPolicy.formatExpiresAtLine(firstNonBlank(
                        rawValues.get("enforcement_expires_at"),
                        rawValues.get("expires_at")
                ))
        );

        return variables;
    }

    public static void validateRequired(EmailNotificationTemplate template, Map<String, String> variables) {
        for (String requiredVariable : template.requiredVariables()) {
            String value = variables.get(requiredVariable);
            if (value == null || value.isBlank()) {
                throw missingVariable(requiredVariable);
            }
            if ("recipient_email".equals(requiredVariable) && !EMAIL_PATTERN.matcher(value).matches()) {
                throw invalidEmail();
            }
        }
    }

    public static String maskEmail(String email) {
        if (email == null || email.isBlank()) {
            return "***";
        }
        int atIndex = email.indexOf('@');
        if (atIndex <= 0) {
            return "***";
        }
        return email.charAt(0) + "***" + email.substring(atIndex);
    }

    private static void putEmailVariable(Map<String, String> variables, String key, String value) {
        if (value == null || value.isBlank()) {
            return;
        }
        variables.put(key, sanitize(value));
    }

    private static void putTextVariable(Map<String, String> variables, String key, String value) {
        if (value == null || value.isBlank()) {
            return;
        }
        variables.put(key, sanitize(value));
    }

    private static void putOptionalNameVariable(Map<String, String> variables, String key, String value) {
        if (value == null || value.isBlank()) {
            variables.put(key, "");
            return;
        }
        variables.put(key, " " + sanitize(value));
    }

    private static String sanitize(String value) {
        String trimmed = value.trim();
        if (trimmed.length() > MAX_VARIABLE_LENGTH) {
            return trimmed.substring(0, MAX_VARIABLE_LENGTH);
        }
        return trimmed
                .replace("<", "")
                .replace(">", "");
    }

    private static String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }

    private static IllegalArgumentException missingVariable(String variable) {
        return new IllegalArgumentException("Missing required template variable: " + variable);
    }

    private static IllegalArgumentException invalidEmail() {
        return new IllegalArgumentException("Invalid recipient email.");
    }

    static Pattern placeholderPattern() {
        return PLACEHOLDER_PATTERN;
    }
}
