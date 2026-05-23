package com.twohands.admin_service.infrastructure.persistence.jpa.repository;

import com.twohands.admin_service.infrastructure.persistence.jpa.entity.SystemConfigEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SystemConfigJpaRepository extends JpaRepository<SystemConfigEntity, UUID> {
	Optional<SystemConfigEntity> findByConfigKey(String configKey);

	boolean existsByConfigKey(String configKey);
}
