package com.twohands.admin_service.application.config.listsystemconfigs;

public record ListSystemConfigsQuery(
		String query,
		String valueType,
		Boolean active,
		Integer page,
		Integer size
) {
}
