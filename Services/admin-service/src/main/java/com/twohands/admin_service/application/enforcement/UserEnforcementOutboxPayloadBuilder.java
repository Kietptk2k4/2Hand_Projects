package com.twohands.admin_service.application.enforcement;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.twohands.admin_service.domain.enforcement.UserEnforcement;
import com.twohands.admin_service.exception.AppException;
import com.twohands.admin_service.exception.ErrorCode;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Component
public class UserEnforcementOutboxPayloadBuilder {

	private final ObjectMapper objectMapper;

	public UserEnforcementOutboxPayloadBuilder(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	public String buildUserSuspendedPayload(UserEnforcement enforcement) {
		return buildEnforcementPayload(enforcement);
	}

	public String buildUserBannedPayload(UserEnforcement enforcement) {
		return buildEnforcementPayload(enforcement);
	}

	public String buildUserRestrictedPayload(UserEnforcement enforcement) {
		return buildEnforcementPayload(enforcement);
	}

	public String buildUserEnforcementExpiredPayload(UserEnforcement enforcement, Instant expiredAt) {
		Map<String, Object> payload = new LinkedHashMap<>();
		payload.put("user_id", enforcement.userId().toString());
		payload.put("enforcement_id", enforcement.id().toString());
		payload.put("action_type", enforcement.actionType().name());
		payload.put("reason_code", enforcement.reasonCode());
		payload.put("previous_status", "ACTIVE");
		payload.put("new_status", enforcement.status().name());
		payload.put("expires_at", enforcement.expiresAt().toString());
		payload.put("expired_at", expiredAt.toString());
		try {
			return objectMapper.writeValueAsString(payload);
		} catch (JsonProcessingException ex) {
			throw new AppException(ErrorCode.INTERNAL_ERROR, "Failed to build outbox payload");
		}
	}

	public String buildUserEnforcementRevokedPayload(
			UserEnforcement enforcement,
			UUID revokedByAdminId,
			String note,
			String reason
	) {
		Map<String, Object> payload = new LinkedHashMap<>();
		payload.put("user_id", enforcement.userId().toString());
		payload.put("enforcement_id", enforcement.id().toString());
		payload.put("action_type", enforcement.actionType().name());
		payload.put("reason_code", enforcement.reasonCode());
		payload.put("previous_status", "ACTIVE");
		payload.put("new_status", enforcement.status().name());
		payload.put("revoked_by", revokedByAdminId.toString());
		if (note != null && !note.isBlank()) {
			payload.put("note", note);
		}
		if (reason != null && !reason.isBlank()) {
			payload.put("revoke_reason", reason);
		}
		try {
			return objectMapper.writeValueAsString(payload);
		} catch (JsonProcessingException ex) {
			throw new AppException(ErrorCode.INTERNAL_ERROR, "Failed to build outbox payload");
		}
	}

	private String buildEnforcementPayload(UserEnforcement enforcement) {
		Map<String, Object> payload = new LinkedHashMap<>();
		payload.put("user_id", enforcement.userId().toString());
		payload.put("enforcement_id", enforcement.id().toString());
		payload.put("action_type", enforcement.actionType().name());
		payload.put("reason_code", enforcement.reasonCode());
		payload.put("description", enforcement.description());
		payload.put("expires_at", enforcement.expiresAt() == null ? null : enforcement.expiresAt().toString());
		payload.put("enforced_by", enforcement.enforcedBy().toString());
		payload.put("status", enforcement.status().name());
		try {
			return objectMapper.writeValueAsString(payload);
		} catch (JsonProcessingException ex) {
			throw new AppException(ErrorCode.INTERNAL_ERROR, "Failed to build outbox payload");
		}
	}
}
