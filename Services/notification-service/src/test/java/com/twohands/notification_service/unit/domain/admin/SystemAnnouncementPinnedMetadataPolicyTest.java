package com.twohands.notification_service.unit.domain.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.twohands.notification_service.domain.admin.SystemAnnouncementPinnedMetadataPolicy;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SystemAnnouncementPinnedMetadataPolicyTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void resolveIsPinned_defaultsFalseWhenMissing() throws Exception {
        assertFalse(SystemAnnouncementPinnedMetadataPolicy.resolveIsPinned(
                objectMapper.readTree("{}")
        ));
    }

    @Test
    void resolveIsPinned_readsIsPinnedField() throws Exception {
        assertTrue(SystemAnnouncementPinnedMetadataPolicy.resolveIsPinned(
                objectMapper.readTree("{\"is_pinned\":true}")
        ));
    }

    @Test
    void resolveIsPinned_readsPinnedAlias() throws Exception {
        assertTrue(SystemAnnouncementPinnedMetadataPolicy.resolveIsPinned(
                objectMapper.readTree("{\"pinned\":true}")
        ));
    }

    @Test
    void resolveIsPinned_throwsWhenShapeInvalid() throws Exception {
        assertThrows(
                IllegalArgumentException.class,
                () -> SystemAnnouncementPinnedMetadataPolicy.resolveIsPinned(
                        objectMapper.readTree("{\"is_pinned\":[]}")
                )
        );
    }
}
