package com.twohands.social_service.domain.integration;

import java.util.UUID;

public interface UserProductAffinityClient {

    UserProductAffinity findByUserId(UUID userId);
}
