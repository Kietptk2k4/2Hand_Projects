package com.twohands.admin_service.domain.enforcement;

import java.util.Optional;
import java.util.UUID;

public interface UserEnforcementRepository {

	UserEnforcement save(UserEnforcement enforcement);

	boolean existsActiveByUserIdAndActionType(UUID userId, UserEnforcementActionType actionType);

	Optional<UserEnforcement> findActiveByUserIdAndActionType(UUID userId, UserEnforcementActionType actionType);

	Optional<UserEnforcement> findById(UUID enforcementId);
}
