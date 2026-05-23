package com.twohands.admin_service.application.config.viewhistory;

import java.util.UUID;

public record ViewSystemConfigHistoryQuery(
		UUID configId,
		Integer page,
		Integer size
) {
}
