package com.twohands.admin_service.infrastructure.persistence.config;

import com.twohands.admin_service.domain.common.PageRequest;
import com.twohands.admin_service.domain.common.PagedResult;
import com.twohands.admin_service.domain.config.SystemConfigHistory;
import com.twohands.admin_service.domain.config.SystemConfigHistoryRepository;
import com.twohands.admin_service.infrastructure.persistence.jpa.entity.SystemConfigHistoryEntity;
import com.twohands.admin_service.infrastructure.persistence.jpa.repository.SystemConfigHistoryJpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

@Repository
public class SystemConfigHistoryRepositoryAdapter implements SystemConfigHistoryRepository {

	private final SystemConfigHistoryJpaRepository jpaRepository;

	public SystemConfigHistoryRepositoryAdapter(SystemConfigHistoryJpaRepository jpaRepository) {
		this.jpaRepository = jpaRepository;
	}

	@Override
	public PagedResult<SystemConfigHistory> findByConfigKeyOrderByCreatedAtDesc(String configKey, PageRequest pageRequest) {
		Pageable pageable = org.springframework.data.domain.PageRequest.of(
				pageRequest.page() - 1,
				pageRequest.size(),
				Sort.by(Sort.Direction.DESC, "createdAt")
		);
		Page<SystemConfigHistoryEntity> page = jpaRepository.findByConfigKeyOrderByCreatedAtDesc(configKey, pageable);
		return new PagedResult<>(
				page.getContent().stream().map(this::toDomain).toList(),
				pageRequest.page(),
				pageRequest.size(),
				page.getTotalElements(),
				page.getTotalPages()
		);
	}

	@Override
	public SystemConfigHistory save(SystemConfigHistory history) {
		SystemConfigHistoryEntity entity = new SystemConfigHistoryEntity();
		entity.setId(history.id());
		entity.setConfigKey(history.configKey());
		entity.setOldValue(history.oldValue());
		entity.setNewValue(history.newValue());
		entity.setChangedBy(history.changedBy());
		entity.setReason(history.reason());
		entity.setCreatedAt(history.createdAt());
		return toDomain(jpaRepository.save(entity));
	}

	private SystemConfigHistory toDomain(SystemConfigHistoryEntity entity) {
		return new SystemConfigHistory(
				entity.getId(),
				entity.getConfigKey(),
				entity.getOldValue(),
				entity.getNewValue(),
				entity.getChangedBy(),
				entity.getReason(),
				entity.getCreatedAt()
		);
	}
}
