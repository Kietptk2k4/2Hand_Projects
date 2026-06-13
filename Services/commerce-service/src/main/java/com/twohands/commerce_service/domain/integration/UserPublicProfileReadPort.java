package com.twohands.commerce_service.domain.integration;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

public interface UserPublicProfileReadPort {

    Map<UUID, UserPublicProfileSummary> findByUserIds(Collection<UUID> userIds);
}
