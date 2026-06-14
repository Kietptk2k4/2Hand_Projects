package com.twohands.notification_service.unit.domain.inapp;

import com.twohands.notification_service.domain.inapp.InAppNotificationTemplatePolicy;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InAppNotificationTemplatePolicyTest {

    @Test
    void resolve_returnsTemplateForSocialEvents() {
        var template = InAppNotificationTemplatePolicy.resolve("POST_LIKED").orElseThrow();

        assertEquals("Thích bài viết", template.title());
        assertEquals("Có người đã thích bài viết của bạn.", template.content());
    }

    @Test
    void resolve_returnsEmptyForUnknownEventType() {
        assertTrue(InAppNotificationTemplatePolicy.resolve("UNKNOWN_EVENT").isEmpty());
    }

    @Test
    void resolve_returnsEmptyForBlankEventType() {
        assertTrue(InAppNotificationTemplatePolicy.resolve("   ").isEmpty());
    }
}
