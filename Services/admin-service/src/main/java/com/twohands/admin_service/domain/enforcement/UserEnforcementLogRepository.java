package com.twohands.admin_service.domain.enforcement;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface UserEnforcementLogRepository {

	UserEnforcementLog save(UserEnforcementLog log);

	List<UserEnforcementLog> findByEnforcementIdsOrderByCreatedAtDesc(Collection<UUID> enforcementIds);
}
