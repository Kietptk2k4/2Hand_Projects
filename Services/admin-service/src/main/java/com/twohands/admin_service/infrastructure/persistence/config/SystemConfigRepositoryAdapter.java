package com.twohands.admin_service.infrastructure.persistence.config;

import com.twohands.admin_service.domain.config.SystemConfig;
import com.twohands.admin_service.domain.config.SystemConfigRepository;
import com.twohands.admin_service.domain.config.SystemConfigValueType;
import com.twohands.admin_service.infrastructure.persistence.jpa.entity.SystemConfigEntity;
import com.twohands.admin_service.infrastructure.persistence.jpa.repository.SystemConfigJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class SystemConfigRepositoryAdapter implements SystemConfigRepository {

	private final SystemConfigJpaRepository jpaRepository;

	public SystemConfigRepositoryAdapter(SystemConfigJpaRepository jpaRepository) {
		this.jpaRepository = jpaRepository;
	}

	@Override
	public SystemConfig save(SystemConfig config) {
		return toDomain(jpaRepository.save(toEntity(config)));
	}

	@Override
	public boolean existsByConfigKey(String configKey) {
		return jpaRepository.existsByConfigKey(configKey);
	}

	@Override
	public Optional<SystemConfig> findByConfigKey(String configKey) {
		return jpaRepository.findByConfigKey(configKey).map(this::toDomain);
	}

	@Override
	public Optional<SystemConfig> findById(UUID configId) {
		return jpaRepository.findById(configId).map(this::toDomain);
	}

	private SystemConfigEntity toEntity(SystemConfig config) {
		SystemConfigEntity entity = new SystemConfigEntity();
		entity.setId(config.id());
		entity.setConfigKey(config.configKey());
		entity.setConfigValue(config.configValue());
		entity.setValueType(com.twohands.admin_service.infrastructure.persistence.jpa.enums.SystemConfigValueType.valueOf(
				config.valueType().name()));
		entity.setDescription(config.description());
		entity.setActive(config.active());
		entity.setCreatedBy(config.createdBy());
		entity.setCreatedAt(config.createdAt());
		entity.setUpdatedBy(config.updatedBy());
		entity.setUpdatedAt(config.updatedAt());
		return entity;
	}

	private SystemConfig toDomain(SystemConfigEntity entity) {
		return new SystemConfig(
				entity.getId(),
				entity.getConfigKey(),
				entity.getConfigValue(),
				SystemConfigValueType.valueOf(entity.getValueType().name()),
				entity.getDescription(),
				entity.isActive(),
				entity.getCreatedBy(),
				entity.getCreatedAt(),
				entity.getUpdatedBy(),
				entity.getUpdatedAt()
		);
	}
}
