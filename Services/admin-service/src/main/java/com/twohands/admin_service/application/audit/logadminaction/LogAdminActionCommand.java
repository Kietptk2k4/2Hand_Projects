package com.twohands.admin_service.application.audit.logadminaction;

import com.twohands.admin_service.domain.audit.AdminActionStatus;

import java.util.Map;
import java.util.UUID;

public record LogAdminActionCommand(
		UUID adminId,
		String actionType,
		String targetType,
		String targetId,
		AdminActionStatus status,
		String message,
		Map<String, Object> requestData,
		Map<String, Object> responseData,
		String ipAddress,
		String userAgent,
		boolean storePayloadOverride
) {
	public LogAdminActionCommand {
		if (targetId == null || targetId.isBlank()) {
			targetId = "N/A";
		}
	}
}
