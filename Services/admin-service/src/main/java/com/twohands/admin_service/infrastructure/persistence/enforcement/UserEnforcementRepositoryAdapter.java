package com.twohands.admin_service.infrastructure.persistence.enforcement;

import com.twohands.admin_service.domain.common.PageRequest;
import com.twohands.admin_service.domain.common.PagedResult;
import com.twohands.admin_service.domain.enforcement.UserEnforcement;
import com.twohands.admin_service.domain.enforcement.UserEnforcementActionType;
import com.twohands.admin_service.domain.enforcement.UserEnforcementRepository;
import com.twohands.admin_service.domain.enforcement.UserEnforcementStatus;
import com.twohands.admin_service.infrastructure.persistence.jpa.entity.UserEnforcementEntity;
import com.twohands.admin_service.infrastructure.persistence.jpa.repository.UserEnforcementJpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.Comparator;
import java.util.List;
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

	@Override
	public List<UserEnforcement> findAllActiveByUserId(UUID userId) {
		return jpaRepository.findByUserIdAndStatus(
				userId,
				com.twohands.admin_service.infrastructure.persistence.jpa.enums.UserEnforcementStatus.ACTIVE
		).stream()
				.map(this::toDomain)
				.sorted(Comparator.comparing(UserEnforcement::createdAt).reversed())
				.toList();
	}

	@Override
	public PagedResult<UserEnforcement> findAllByUserId(UUID userId, PageRequest pageRequest) {
		Pageable pageable = org.springframework.data.domain.PageRequest.of(
				pageRequest.page() - 1,
				pageRequest.size(),
				Sort.by(Sort.Direction.DESC, "createdAt")
		);
		Page<UserEnforcementEntity> page = jpaRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
		return new PagedResult<>(
				page.getContent().stream().map(this::toDomain).toList(),
				pageRequest.page(),
				pageRequest.size(),
				page.getTotalElements(),
				page.getTotalPages()
		);
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
