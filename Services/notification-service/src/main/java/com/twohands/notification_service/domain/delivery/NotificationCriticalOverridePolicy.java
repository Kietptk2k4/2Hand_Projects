package com.twohands.notification_service.domain.delivery;

import java.util.Set;

public final class NotificationCriticalOverridePolicy {

    private static final Set<String> SECURITY_CRITICAL_EMAIL = Set.of(
            "EMAIL_VERIFICATION_REQUESTED",
            "PASSWORD_RESET_REQUESTED",
            "PASSWORD_CHANGED"
    );

    private static final Set<String> ACCOUNT_CRITICAL = Set.of(
            "USER_SUSPENDED",
            "USER_BANNED",
            "USER_RESTRICTED",
            "SHOP_SUSPENDED",
            "PASSWORD_CHANGED"
    );

    private static final Set<String> MANDATORY_IN_APP = Set.of(
            "SYSTEM_ANNOUNCEMENT_SENT"
    );

    private NotificationCriticalOverridePolicy() {
    }

    public static boolean isSecurityCriticalEmail(String eventType) {
        return SECURITY_CRITICAL_EMAIL.contains(eventType);
    }

    public static boolean isAccountCritical(String eventType) {
        return ACCOUNT_CRITICAL.contains(eventType);
    }

    public static boolean isMandatoryInApp(String eventType) {
        return MANDATORY_IN_APP.contains(eventType);
    }

    public static boolean forcesEmail(String eventType) {
        return isSecurityCriticalEmail(eventType) || isAccountCritical(eventType);
    }

    public static boolean forcesPush(String eventType) {
        return isAccountCritical(eventType);
    }
}
