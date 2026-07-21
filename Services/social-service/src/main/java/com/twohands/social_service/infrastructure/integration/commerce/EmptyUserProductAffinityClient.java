package com.twohands.social_service.infrastructure.integration.commerce;

import com.twohands.social_service.domain.integration.UserProductAffinity;
import com.twohands.social_service.domain.integration.UserProductAffinityClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Phase 1 default: empty affinity (cross_domain = 0) until Commerce purchase-profile API is available.
 * Offline Python build-dataset may still compute affinity from Commerce DB extracts.
 */
@Component
public class EmptyUserProductAffinityClient implements UserProductAffinityClient {

    private static final Logger log = LoggerFactory.getLogger(EmptyUserProductAffinityClient.class);

    @Override
    public UserProductAffinity findByUserId(UUID userId) {
        if (userId != null) {
            log.trace("No purchase affinity source configured; returning empty profile for user={}", userId);
        }
        return UserProductAffinity.empty();
    }
}
