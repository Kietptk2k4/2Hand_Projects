package com.twohands.social_service.domain.user;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserProjectionRepository {
    Optional<UserProjection> findByUserId(UUID userId);

    List<UserProjection> findByUserIds(List<UUID> userIds);

    List<UserProjection> findActiveSuggestionCandidatesExcluding(Collection<String> excludeUserIds, int maxResults);

    UserProjection upsert(UserProjection projection);
}
