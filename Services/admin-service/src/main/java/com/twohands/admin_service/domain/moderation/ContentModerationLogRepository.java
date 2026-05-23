package com.twohands.admin_service.domain.moderation;

import com.twohands.admin_service.domain.common.PageRequest;
import com.twohands.admin_service.domain.common.PagedResult;

public interface ContentModerationLogRepository {

	ContentModerationLog save(ContentModerationLog log);

	PagedResult<ContentModerationLog> findByTargetTypeAndTargetIdOrderByCreatedAtDesc(
			ContentModerationTargetType targetType,
			String targetId,
			PageRequest pageRequest
	);
}
