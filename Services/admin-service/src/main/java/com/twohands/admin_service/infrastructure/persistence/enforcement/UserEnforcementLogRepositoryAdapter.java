package com.twohands.admin_service.infrastructure.persistence.enforcement;

import com.twohands.admin_service.domain.enforcement.UserEnforcementLog;
import com.twohands.admin_service.domain.enforcement.UserEnforcementLogRepository;
import com.twohands.admin_service.domain.enforcement.UserEnforcementStatus;
import com.twohands.admin_service.infrastructure.persistence.jpa.entity.UserEnforcementEntity;
import com.twohands.admin_service.infrastructure.persistence.jpa.entity.UserEnforcementLogEntity;
import com.twohands.admin_service.infrastructure.persistence.jpa.repository.UserEnforcementJpaRepository;
import com.twohands.admin_service.infrastructure.persistence.jpa.repository.UserEnforcementLogJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public class UserEnforcementLogRepositoryAdapter implements UserEnforcementLogRepository {

	private final UserEnforcementLogJpaRepository logJpaRepository;
	private final UserEnforcementJpaRepository enforcementJpaRepository;

	public UserEnforcementLogRepositoryAdapter(
			UserEnforcementLogJpaRepository logJpaRepository,
			UserEnforcementJpaRepository enforcementJpaRepository
	) {
		this.logJpaRepository = logJpaRepository;
		this.enforcementJpaRepository = enforcementJpaRepository;
	}

	@Override
	public UserEnforcementLog save(UserEnforcementLog log) {
		UserEnforcementLogEntity entity = new UserEnforcementLogEntity();
		entity.setId(log.id());
		UserEnforcementEntity enforcement = enforcementJpaRepository.findById(log.enforcementId())
				.orElseThrow();
		entity.setEnforcement(enforcement);
		if (log.oldStatus() != null) {
			entity.setOldStatus(com.twohands.admin_service.infrastructure.persistence.jpa.enums.UserEnforcementStatus.valueOf(
					log.oldStatus().name()));
		}
		entity.setNewStatus(com.twohands.admin_service.infrastructure.persistence.jpa.enums.UserEnforcementStatus.valueOf(
				log.newStatus().name()));
		entity.setAdminId(log.adminId());
		entity.setNote(log.note());
		entity.setCreatedAt(log.createdAt());
		return toDomain(logJpaRepository.save(entity));
	}

	@Override
	public List<UserEnforcementLog> findByEnforcementIdsOrderByCreatedAtDesc(Collection<UUID> enforcementIds) {
		if (enforcementIds == null || enforcementIds.isEmpty()) {
			return List.of();
		}
		return logJpaRepository.findByEnforcementIdsOrderByCreatedAtDesc(enforcementIds).stream()
				.map(this::toDomain)
				.toList();
	}

	private UserEnforcementLog toDomain(UserEnforcementLogEntity entity) {
		return new UserEnforcementLog(
				entity.getId(),
				entity.getEnforcement().getId(),
				entity.getOldStatus() == null
						? null
						: UserEnforcementStatus.valueOf(entity.getOldStatus().name()),
				UserEnforcementStatus.valueOf(entity.getNewStatus().name()),
				entity.getAdminId(),
				entity.getNote(),
				entity.getCreatedAt()
		);
	}
}
