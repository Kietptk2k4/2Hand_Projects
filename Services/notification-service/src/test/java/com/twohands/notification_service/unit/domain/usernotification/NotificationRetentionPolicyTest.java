package com.twohands.notification_service.unit.domain.usernotification;

import com.twohands.notification_service.domain.usernotification.NotificationRetentionPolicy;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NotificationRetentionPolicyTest {

    @Test
    void isRetentionDaysValid_requiresPositiveDays() {
        assertTrue(NotificationRetentionPolicy.isRetentionDaysValid(180));
        assertFalse(NotificationRetentionPolicy.isRetentionDaysValid(0));
        assertFalse(NotificationRetentionPolicy.isRetentionDaysValid(-1));
    }

    @Test
    void isRetainedNotificationType_protectsCriticalTypes() {
        assertTrue(NotificationRetentionPolicy.isRetainedNotificationType("USER_SUSPENDED"));
        assertTrue(NotificationRetentionPolicy.isRetainedNotificationType("USER_BANNED"));
        assertTrue(NotificationRetentionPolicy.isRetainedNotificationType("SYSTEM_ANNOUNCEMENT_SENT"));
        assertFalse(NotificationRetentionPolicy.isRetainedNotificationType("POST_LIKED"));
    }

    @Test
    void isRetainedReferenceType_protectsSystemAnnouncements() {
        assertTrue(NotificationRetentionPolicy.isRetainedReferenceType("SYSTEM_ANNOUNCEMENT"));
        assertFalse(NotificationRetentionPolicy.isRetainedReferenceType("ORDER"));
    }
}
