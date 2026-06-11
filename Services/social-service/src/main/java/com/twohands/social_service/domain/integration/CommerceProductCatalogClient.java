package com.twohands.social_service.domain.integration;

import java.util.Optional;

public interface CommerceProductCatalogClient {

    Optional<CommerceProductSnapshot> findVisibleProductSnapshot(String productId);
}
