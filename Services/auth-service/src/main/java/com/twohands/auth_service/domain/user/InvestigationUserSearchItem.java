package com.twohands.auth_service.domain.user;

import java.util.List;
import java.util.UUID;

public record InvestigationUserSearchItem(
		UUID userId,
		String email,
		String displayName,
		String status,
		List<String> roleCodes
) {
}
