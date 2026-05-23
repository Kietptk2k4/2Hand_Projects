package com.twohands.admin_service.infrastructure.persistence.jpa.repository;

import com.twohands.admin_service.infrastructure.persistence.jpa.entity.UserEnforcementEntity;
import com.twohands.admin_service.infrastructure.persistence.jpa.enums.UserEnforcementStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface UserEnforcementJpaRepository extends JpaRepository<UserEnforcementEntity, UUID> {
	List<UserEnforcementEntity> findByUserIdAndStatus(UUID userId, UserEnforcementStatus status);

	boolean existsByUserIdAndActionTypeAndStatus(
			UUID userId,
			com.twohands.admin_service.infrastructure.persistence.jpa.enums.UserEnforcementActionType actionType,
			UserEnforcementStatus status
	);

	List<UserEnforcementEntity> findByUserIdAndActionTypeAndStatus(
			UUID userId,
			com.twohands.admin_service.infrastructure.persistence.jpa.enums.UserEnforcementActionType actionType,
			UserEnforcementStatus status
	);
}
