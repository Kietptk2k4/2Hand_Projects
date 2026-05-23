package com.twohands.admin_service.domain.audit;

import com.twohands.admin_service.domain.common.PageRequest;
import com.twohands.admin_service.domain.common.PagedResult;

import java.util.Optional;
import java.util.UUID;

public interface AdminActionLogRepository {

	AdminActionLog save(AdminActionLog log);

	Optional<AdminActionLog> findById(UUID logId);

	PagedResult<AdminActionLog> search(AdminActionLogSearchCriteria criteria, PageRequest pageRequest);
}
