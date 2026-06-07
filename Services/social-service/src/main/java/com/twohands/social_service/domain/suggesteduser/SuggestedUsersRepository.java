package com.twohands.social_service.domain.suggesteduser;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

public interface SuggestedUsersRepository {
    Map<UUID, Long> findMutualFollowCounts(UUID viewerId, Collection<UUID> candidateUserIds);
}