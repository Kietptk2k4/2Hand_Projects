package com.twohands.admin_service.infrastructure.persistence.enforcement;

import com.twohands.admin_service.domain.enforcement.UserEnforcement;
import com.twohands.admin_service.domain.enforcement.UserEnforcementActionType;
import com.twohands.admin_service.domain.enforcement.UserEnforcementRepository;
import com.twohands.admin_service.domain.enforcement.UserEnforcementStatus;
import com.twohands.admin_service.infrastructure.persistence.jpa.entity.UserEnforcementEntity;
import com.twohands.admin_service.infrastructure.persistence.jpa.repository.UserEnforcementJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class UserEnforcementRepositoryAdapter implements UserEnforcementRepository {

	private final UserEnforcementJpaRepository jpaRepository;

	public UserEnforcementRepositoryAdapter(UserEnforcementJpaRepository jpaRepository) {
		this.jpaRepository = jpaRepository;
	}

	@Override
	public UserEnforcement save(UserEnforcement enforcement) {
		return toDomain(jpaRepository.save(toEntity(enforcement)));
	}

	@Override
	public boolean existsActiveByUserIdAndActionType(UUID userId, UserEnforcementActionType actionType) {
		return jpaRepository.existsByUserIdAndActionTypeAndStatus(
				userId,
				toJpaActionType(actionType),
				com.twohands.admin_service.infrastructure.persistence.jpa.enums.UserEnforcementStatus.ACTIVE
		);
	}

	@Override
	public Optional<UserEnforcement> findActiveByUserIdAndActionType(UUID userId, UserEnforcementActionType actionType) {
		return jpaRepository.findByUserIdAndActionTypeAndStatus(
				userId,
				toJpaActionType(actionType),
				com.twohands.admin_service.infrastructure.persistence.jpa.enums.UserEnforcementStatus.ACTIVE
		).stream().findFirst().map(this::toDomain);
	}

	@Override
	public Optional<UserEnforcement> findById(UUID enforcementId) {
		return jpaRepository.findById(enforcementId).map(this::toDomain);
	}

	private UserEnforcementEntity toEntity(UserEnforcement enforcement) {
		UserEnforcementEntity entity = new UserEnforcementEntity();
		entity.setId(enforcement.id());
		entity.setUserId(enforcement.userId());
		entity.setActionType(toJpaActionType(enforcement.actionType()));
		entity.setReasonCode(enforcement.reasonCode());
		entity.setDescription(enforcement.description());
		entity.setExpiresAt(enforcement.expiresAt());
		entity.setEnforcedBy(enforcement.enforcedBy());
		entity.setStatus(com.twohands.admin_service.infrastructure.persistence.jpa.enums.UserEnforcementStatus.valueOf(
				enforcement.status().name()));
		entity.setCreatedAt(enforcement.createdAt());
		entity.setUpdatedAt(enforcement.updatedAt());
		return entity;
	}

	private UserEnforcement toDomain(UserEnforcementEntity entity) {
		return new UserEnforcement(
				entity.getId(),
				entity.getUserId(),
				UserEnforcementActionType.valueOf(entity.getActionType().name()),
				entity.getReasonCode(),
				entity.getDescription(),
				entity.getExpiresAt(),
				entity.getEnforcedBy(),
				UserEnforcementStatus.valueOf(entity.getStatus().name()),
				entity.getCreatedAt(),
				entity.getUpdatedAt()
		);
	}

	private com.twohands.admin_service.infrastructure.persistence.jpa.enums.UserEnforcementActionType toJpaActionType(
			UserEnforcementActionType actionType
	) {
		return com.twohands.admin_service.infrastructure.persistence.jpa.enums.UserEnforcementActionType.valueOf(
				actionType.name());
	}
}
