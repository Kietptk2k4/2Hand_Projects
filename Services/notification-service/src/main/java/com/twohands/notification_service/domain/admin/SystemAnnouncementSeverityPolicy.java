package com.twohands.notification_service.domain.admin;

public final class SystemAnnouncementSeverityPolicy {

    private SystemAnnouncementSeverityPolicy() {
    }

    public static boolean requiresPush(String severity) {
        return severity != null && "CRITICAL".equalsIgnoreCase(severity.trim());
    }
}
