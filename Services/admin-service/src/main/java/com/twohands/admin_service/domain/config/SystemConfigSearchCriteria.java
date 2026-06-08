package com.twohands.admin_service.domain.config;

public record SystemConfigSearchCriteria(
		String query,
		SystemConfigValueType valueType,
		Boolean active
) {
}
