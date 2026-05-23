package com.twohands.admin_service.application.audit;

import com.twohands.admin_service.application.audit.viewlogs.AdminActionLogItem;
import com.twohands.admin_service.domain.audit.AdminActionLog;
import com.twohands.admin_service.domain.audit.AuditPayloadSanitizer;
import org.springframework.stereotype.Component;

@Component
public class AdminActionLogResponseMapper {

	private final AuditPayloadSanitizer auditPayloadSanitizer;

	public AdminActionLogResponseMapper(AuditPayloadSanitizer auditPayloadSanitizer) {
		this.auditPayloadSanitizer = auditPayloadSanitizer;
	}

	public AdminActionLogItem toItem(AdminActionLog log) {
		return new AdminActionLogItem(
				log.id(),
				log.adminId(),
				log.actionType(),
				log.targetType(),
				log.targetId(),
				log.status(),
				sanitizePayload(log.requestPayloadJson()),
				sanitizePayload(log.responsePayloadJson()),
				log.ipAddress(),
				log.userAgent(),
				log.createdAt()
		);
	}

	private String sanitizePayload(String payload) {
		if (payload == null || payload.isBlank()) {
			return null;
		}
		return auditPayloadSanitizer.sanitizeJson(payload);
	}
}
