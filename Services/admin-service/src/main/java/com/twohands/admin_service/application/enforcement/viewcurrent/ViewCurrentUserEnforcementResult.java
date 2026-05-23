package com.twohands.admin_service.application.enforcement.viewcurrent;

import java.util.List;
import java.util.UUID;

public record ViewCurrentUserEnforcementResult(
		UUID userId,
		List<CurrentUserEnforcementItem> enforcements
) {
}
