package com.twohands.commerce_service.unit.infrastructure.ghn;

import com.twohands.commerce_service.config.CommerceIntegrationProperties;
import com.twohands.commerce_service.infrastructure.ghn.GhnTrackSyncCooldownRegistry;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class GhnTrackSyncCooldownRegistryTest {

    @Test
    void shouldRespectCooldownUnlessForced() {
        CommerceIntegrationProperties properties = new CommerceIntegrationProperties();
        properties.getGhn().setTrackSyncCooldownSeconds(300);
        Clock clock = Clock.fixed(Instant.parse("2026-06-07T10:00:00Z"), ZoneOffset.UTC);
        GhnTrackSyncCooldownRegistry registry = new GhnTrackSyncCooldownRegistry(properties, clock);
        UUID shipmentId = UUID.randomUUID();

        assertThat(registry.shouldSync(shipmentId, false)).isTrue();
        registry.markSynced(shipmentId);
        assertThat(registry.shouldSync(shipmentId, false)).isFalse();
        assertThat(registry.shouldSync(shipmentId, true)).isTrue();
    }
}
