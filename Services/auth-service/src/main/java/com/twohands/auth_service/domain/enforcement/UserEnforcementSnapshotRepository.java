package com.twohands.auth_service.domain.enforcement;

import java.util.Optional;
import java.util.UUID;

public interface UserEnforcementSnapshotRepository {

    Optional<UserEnforcementSnapshot> findByEnforcementId(UUID enforcementId);

    Optional<UserEnforcementSnapshot> findByEventId(UUID eventId);

    boolean existsAppliedBlockingEnforcement(UUID userId);

    UserEnforcementSnapshot save(UserEnforcementSnapshot snapshot);

    int markStatus(UUID enforcementId, UserEnforcementSnapshotStatus status, java.time.Instant updatedAt);
}
