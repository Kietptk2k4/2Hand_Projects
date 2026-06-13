package com.twohands.commerce_service.infrastructure.integration.auth;

import com.twohands.commerce_service.domain.integration.UserPublicProfileReadPort;
import com.twohands.commerce_service.domain.integration.UserPublicProfileSummary;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

@Component
@ConditionalOnProperty(name = "commerce.integrations.auth.enabled", havingValue = "false")
public class DisabledUserPublicProfileReadAdapter implements UserPublicProfileReadPort {

    @Override
    public Map<UUID, UserPublicProfileSummary> findByUserIds(Collection<UUID> userIds) {
        return Map.of();
    }
}
