package com.twohands.admin_service.delivery.http.enforcement;

import jakarta.validation.constraints.Size;

public record RevokeUserEnforcementRequest(
		@Size(max = 4000)
		String note,
		@Size(max = 4000)
		String reason
) {
}
