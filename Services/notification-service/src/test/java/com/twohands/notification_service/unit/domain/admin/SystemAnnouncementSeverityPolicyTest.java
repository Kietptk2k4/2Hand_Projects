package com.twohands.notification_service.unit.domain.admin;

import com.twohands.notification_service.domain.admin.SystemAnnouncementSeverityPolicy;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SystemAnnouncementSeverityPolicyTest {

    @Test
    void requiresPush_onlyForCritical() {
        assertTrue(SystemAnnouncementSeverityPolicy.requiresPush("CRITICAL"));
        assertFalse(SystemAnnouncementSeverityPolicy.requiresPush("WARNING"));
        assertFalse(SystemAnnouncementSeverityPolicy.requiresPush("INFO"));
        assertFalse(SystemAnnouncementSeverityPolicy.requiresPush(null));
    }
}
