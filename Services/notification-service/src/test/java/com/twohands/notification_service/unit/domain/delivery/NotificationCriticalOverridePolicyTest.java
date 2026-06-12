package com.twohands.notification_service.unit.domain.delivery;

import com.twohands.notification_service.domain.delivery.NotificationCriticalOverridePolicy;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NotificationCriticalOverridePolicyTest {

    @Test
    void forcesEmailForSecurityCriticalEvents() {
        assertTrue(NotificationCriticalOverridePolicy.forcesEmail("EMAIL_VERIFICATION_REQUESTED"));
        assertTrue(NotificationCriticalOverridePolicy.forcesEmail("PASSWORD_RESET_REQUESTED"));
        assertTrue(NotificationCriticalOverridePolicy.forcesEmail("USER_SUSPENDED"));
        assertTrue(NotificationCriticalOverridePolicy.forcesEmail("USER_BANNED"));
        assertFalse(NotificationCriticalOverridePolicy.forcesEmail("POST_LIKED"));
    }

    @Test
    void forcesPushForAccountCriticalEvents() {
        assertTrue(NotificationCriticalOverridePolicy.forcesPush("USER_SUSPENDED"));
        assertTrue(NotificationCriticalOverridePolicy.forcesPush("USER_BANNED"));
        assertFalse(NotificationCriticalOverridePolicy.forcesPush("POST_LIKED"));
    }

    @Test
    void forcesInAppForMandatoryAnnouncement() {
        assertTrue(NotificationCriticalOverridePolicy.isMandatoryInApp("SYSTEM_ANNOUNCEMENT_SENT"));
        assertFalse(NotificationCriticalOverridePolicy.isMandatoryInApp("POST_LIKED"));
    }
}
