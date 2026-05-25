package com.twohands.notification_service.unit.application.delivery;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.twohands.notification_service.application.delivery.PushDeliveryRetryMetadataCodec;
import com.twohands.notification_service.application.worker.NotificationFailurePolicy;
import com.twohands.notification_service.domain.delivery.PushDeliveryRetryState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PushDeliveryRetryMetadataCodecTest {

    private PushDeliveryRetryMetadataCodec codec;

    @BeforeEach
    void setUp() {
        codec = new PushDeliveryRetryMetadataCodec(new ObjectMapper());
    }

    @Test
    void parseAndMerge_roundTripPushDeliveryState() {
        var state = new PushDeliveryRetryState(
                NotificationFailurePolicy.RETRYABLE,
                2,
                5,
                "FCM timeout",
                Instant.parse("2026-05-20T08:00:00Z")
        );

        String metadata = codec.mergePushDeliveryState("{\"pinned\":true}", state);
        var parsed = codec.parse(metadata);

        assertTrue(parsed.isPresent());
        assertEquals(NotificationFailurePolicy.RETRYABLE, parsed.get().failurePolicy());
        assertEquals(2, parsed.get().retryCount());
        assertEquals("FCM timeout", parsed.get().lastError());
        assertTrue(metadata.contains("\"pinned\":true"));
    }

    @Test
    void clearPushDeliveryState_removesPushDeliveryNode() {
        var state = new PushDeliveryRetryState(
                NotificationFailurePolicy.RETRYABLE,
                1,
                5,
                "error",
                Instant.parse("2026-05-20T08:00:00Z")
        );
        String withPush = codec.mergePushDeliveryState("{}", state);
        String cleared = codec.clearPushDeliveryState(withPush);

        assertFalse(cleared.contains("pushDelivery"));
    }
}
