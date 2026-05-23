package com.twohands.admin_service.domain.enforcement;

import com.twohands.admin_service.domain.common.PageRequest;
import com.twohands.admin_service.domain.common.PagedResult;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserEnforcementRepository {

	UserEnforcement save(UserEnforcement enforcement);

	boolean existsActiveByUserIdAndActionType(UUID userId, UserEnforcementActionType actionType);

	Optional<UserEnforcement> findActiveByUserIdAndActionType(UUID userId, UserEnforcementActionType actionType);

	Optional<UserEnforcement> findById(UUID enforcementId);

	List<UserEnforcement> findAllActiveByUserId(UUID userId);

	PagedResult<UserEnforcement> findAllByUserId(UUID userId, PageRequest pageRequest);
}
