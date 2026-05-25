package com.twohands.notification_service.unit.domain.push;

import com.twohands.notification_service.domain.push.PushNotificationPayloadPolicy;
import com.twohands.notification_service.domain.push.PushNotificationTemplate;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class PushNotificationPayloadPolicyTest {

    @Test
    void build_includesSafeDataFields() {
        UUID eventId = UUID.randomUUID();

        var payload = PushNotificationPayloadPolicy.build(
                new PushNotificationTemplate("New like", "Someone liked your post."),
                "POST_LIKED",
                "POST",
                "post-123",
                eventId
        );

        assertEquals("New like", payload.title());
        assertEquals("Someone liked your post.", payload.body());
        assertEquals("POST_LIKED", payload.data().get("eventType"));
        assertEquals("POST", payload.data().get("referenceType"));
        assertEquals("post-123", payload.data().get("referenceId"));
        assertEquals(eventId.toString(), payload.data().get("notificationEventId"));
    }

    @Test
    void build_omitsBlankReferenceFields() {
        var payload = PushNotificationPayloadPolicy.build(
                new PushNotificationTemplate("Title", "Body"),
                "REVIEW_HIDDEN",
                "",
                null,
                null
        );

        assertEquals("REVIEW_HIDDEN", payload.data().get("eventType"));
        assertFalse(payload.data().containsKey("referenceType"));
        assertFalse(payload.data().containsKey("referenceId"));
        assertFalse(payload.data().containsKey("notificationEventId"));
    }

    @Test
    void build_truncatesLongTitleAndBody() {
        String longTitle = "T".repeat(121);
        String longBody = "B".repeat(241);

        var payload = PushNotificationPayloadPolicy.build(
                new PushNotificationTemplate(longTitle, longBody),
                "ORDER_CREATED",
                "ORDER",
                "order-1",
                null
        );

        assertEquals(120, payload.title().length());
        assertEquals(240, payload.body().length());
        assertEquals("T", payload.title().substring(0, 1));
        assertEquals("B", payload.body().substring(0, 1));
    }
}
