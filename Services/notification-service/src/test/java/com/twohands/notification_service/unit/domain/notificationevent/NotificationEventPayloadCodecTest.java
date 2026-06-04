package com.twohands.notification_service.unit.domain.notificationevent;

import com.twohands.notification_service.domain.notificationevent.NotificationEventPayloadCodec;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NotificationEventPayloadCodecTest {

    @Test
    void decode_unwrapsJsonEncodedStringPayload() {
        String decoded = NotificationEventPayloadCodec.decode(
                "\"{\\\"post_author_id\\\":\\\"11111111-1111-1111-1111-111111111111\\\","
                        + "\\\"post_id\\\":\\\"post-100\\\"}\""
        );

        assertTrue(decoded.contains("post_author_id"));
        assertTrue(decoded.contains("post-100"));
        assertFalse(decoded.startsWith("\""));
    }

    @Test
    void decode_preservesObjectPayload() {
        String decoded = NotificationEventPayloadCodec.decode(
                """
                        {"verification_code":"123456","recipient_email":"user@example.com"}
                        """
        );

        assertTrue(decoded.contains("\"verification_code\":\"123456\""));
    }
}
