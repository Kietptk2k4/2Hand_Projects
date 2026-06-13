package com.twohands.notification_service.unit.domain.social;

import com.twohands.notification_service.domain.inapp.InAppNotificationTemplate;
import com.twohands.notification_service.domain.social.SocialNotificationTemplatePolicy;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SocialNotificationTemplatePolicyTest {

    @Test
    void postCreatedInApp_usesActorNameWhenPresent() {
        InAppNotificationTemplate template = SocialNotificationTemplatePolicy.postCreatedInApp("Alice");

        assertEquals("New post", template.title());
        assertEquals("Alice shared a new post.", template.content());
    }

    @Test
    void postCreatedInApp_fallsBackWhenNameMissing() {
        InAppNotificationTemplate template = SocialNotificationTemplatePolicy.postCreatedInApp(null);

        assertEquals("Someone you follow shared a new post.", template.content());
    }

    @Test
    void avatarUpdatedInApp_usesActorNameWhenPresent() {
        InAppNotificationTemplate template = SocialNotificationTemplatePolicy.avatarUpdatedInApp("Bob");

        assertEquals("Avatar updated", template.title());
        assertEquals("Bob updated their profile photo.", template.content());
    }
}
