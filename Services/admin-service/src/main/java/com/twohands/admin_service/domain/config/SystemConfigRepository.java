package com.twohands.admin_service.domain.config;

import java.util.Optional;
import java.util.UUID;

public interface SystemConfigRepository {

	SystemConfig save(SystemConfig config);

	boolean existsByConfigKey(String configKey);

	Optional<SystemConfig> findByConfigKey(String configKey);

	Optional<SystemConfig> findById(UUID configId);
}
