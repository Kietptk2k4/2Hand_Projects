package com.twohands.admin_service.domain.config;

import com.twohands.admin_service.domain.common.PageRequest;
import com.twohands.admin_service.domain.common.PagedResult;

public interface SystemConfigHistoryRepository {

	SystemConfigHistory save(SystemConfigHistory history);

	PagedResult<SystemConfigHistory> findByConfigKeyOrderByCreatedAtDesc(String configKey, PageRequest pageRequest);
}
