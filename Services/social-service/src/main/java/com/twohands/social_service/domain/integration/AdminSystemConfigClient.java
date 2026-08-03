package com.twohands.social_service.domain.integration;

import java.util.Optional;

public interface AdminSystemConfigClient {

    /**
     * Returns the active config value for an exact config key, or empty if missing / unreachable.
     */
    Optional<String> findActiveConfigValue(String configKey);
}
