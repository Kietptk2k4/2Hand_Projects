package com.twohands.notification_service.unit.domain.social;

import com.twohands.notification_service.domain.inapp.InAppNotificationTemplate;
import com.twohands.notification_service.domain.social.SocialNotificationTemplatePolicy;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SocialNotificationTemplatePolicyTest {

    @Test
    void postCreatedInApp_usesActorNameWhenPresent() {
        InAppNotificationTemplate template = SocialNotificationTemplatePolicy.postCreatedInApp("Alice");

        assertEquals("Bài viết mới", template.title());
        assertEquals("Alice đã đăng bài viết mới.", template.content());
    }

    @Test
    void postCreatedInApp_fallsBackWhenNameMissing() {
        InAppNotificationTemplate template = SocialNotificationTemplatePolicy.postCreatedInApp(null);

        assertEquals("Người bạn đang theo dõi đã đăng bài viết mới.", template.content());
    }

    @Test
    void avatarUpdatedInApp_usesActorNameWhenPresent() {
        InAppNotificationTemplate template = SocialNotificationTemplatePolicy.avatarUpdatedInApp("Bob");

        assertEquals("Cập nhật ảnh đại diện", template.title());
        assertEquals("Bob đã cập nhật ảnh đại diện.", template.content());
    }
}
