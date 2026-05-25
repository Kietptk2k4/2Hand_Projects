package com.twohands.notification_service.unit.application.delivery;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.twohands.notification_service.application.delivery.EmailDeliveryRetryMetadataCodec;
import com.twohands.notification_service.application.worker.NotificationFailurePolicy;
import com.twohands.notification_service.domain.delivery.EmailDeliveryRetryState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EmailDeliveryRetryMetadataCodecTest {

    private EmailDeliveryRetryMetadataCodec codec;

    @BeforeEach
    void setUp() {
        codec = new EmailDeliveryRetryMetadataCodec(new ObjectMapper());
    }

    @Test
    void parseAndMerge_roundTripEmailDeliveryState() {
        var state = new EmailDeliveryRetryState(
                NotificationFailurePolicy.RETRYABLE,
                2,
                5,
                "Email provider timeout.",
                Instant.parse("2026-05-20T08:00:00Z")
        );

        String metadata = codec.mergeEmailDeliveryState("{\"pinned\":true}", state);
        var parsed = codec.parse(metadata);

        assertTrue(parsed.isPresent());
        assertEquals(NotificationFailurePolicy.RETRYABLE, parsed.get().failurePolicy());
        assertEquals(2, parsed.get().retryCount());
        assertTrue(metadata.contains("\"pinned\":true"));
    }

    @Test
    void clearEmailDeliveryState_removesEmailDeliveryNode() {
        var state = new EmailDeliveryRetryState(
                NotificationFailurePolicy.RETRYABLE,
                1,
                5,
                "error",
                Instant.parse("2026-05-20T08:00:00Z")
        );
        String withEmail = codec.mergeEmailDeliveryState("{}", state);
        String cleared = codec.clearEmailDeliveryState(withEmail);

        assertFalse(cleared.contains("emailDelivery"));
    }
}
