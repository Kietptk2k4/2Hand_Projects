package com.twohands.notification_service.domain.email;

import java.util.Map;
import java.util.regex.Matcher;

public final class EmailNotificationContentRenderer {

    private EmailNotificationContentRenderer() {
    }

    public static EmailNotificationContent render(
            EmailNotificationTemplate template,
            Map<String, String> variables
    ) {
        String to = variables.get("recipient_email");
        String subject = renderTemplate(template.subjectTemplate(), variables);
        String body = renderTemplate(template.bodyTemplate(), variables);
        return new EmailNotificationContent(to, subject, body);
    }

    private static String renderTemplate(String template, Map<String, String> variables) {
        Matcher matcher = EmailNotificationVariablesPolicy.placeholderPattern().matcher(template);
        StringBuilder rendered = new StringBuilder();
        while (matcher.find()) {
            String key = matcher.group(1);
            String value = variables.getOrDefault(key, "");
            matcher.appendReplacement(rendered, Matcher.quoteReplacement(value));
        }
        matcher.appendTail(rendered);
        return rendered.toString().trim();
    }
}
