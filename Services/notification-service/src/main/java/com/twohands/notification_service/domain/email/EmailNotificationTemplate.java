package com.twohands.notification_service.domain.email;

import java.util.Set;

public record EmailNotificationTemplate(
        String subjectTemplate,
        String bodyTemplate,
        Set<String> requiredVariables
) {
}
