package com.twohands.commerce_service.infrastructure.ghn;

import com.twohands.commerce_service.config.CommerceIntegrationProperties;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class GhnTrackSyncCooldownRegistry {

    private final int cooldownSeconds;
    private final Clock clock;
    private final Map<UUID, Instant> lastSyncedAt = new ConcurrentHashMap<>();

    public GhnTrackSyncCooldownRegistry(
            CommerceIntegrationProperties integrationProperties,
            Clock clock
    ) {
        this.cooldownSeconds = integrationProperties.getGhn().getTrackSyncCooldownSeconds();
        this.clock = clock;
    }

    public boolean shouldSync(UUID shipmentId, boolean force) {
        if (force || cooldownSeconds <= 0) {
            return true;
        }
        Instant lastSynced = lastSyncedAt.get(shipmentId);
        if (lastSynced == null) {
            return true;
        }
        return Duration.between(lastSynced, clock.instant()).getSeconds() >= cooldownSeconds;
    }

    public void markSynced(UUID shipmentId) {
        lastSyncedAt.put(shipmentId, clock.instant());
    }
}
