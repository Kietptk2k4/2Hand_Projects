package com.twohands.admin_service.application.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.twohands.admin_service.domain.config.SystemConfig;
import com.twohands.admin_service.exception.AppException;
import com.twohands.admin_service.exception.ErrorCode;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class SystemConfigOutboxPayloadBuilder {

	private final ObjectMapper objectMapper;

	public SystemConfigOutboxPayloadBuilder(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	public String buildSystemConfigUpdatedPayload(SystemConfig config, String changeType) {
		Map<String, Object> payload = new LinkedHashMap<>();
		payload.put("config_id", config.id().toString());
		payload.put("config_key", config.configKey());
		payload.put("value_type", config.valueType().name());
		payload.put("config_value", config.configValue());
		payload.put("is_active", config.active());
		payload.put("change_type", changeType);
		try {
			return objectMapper.writeValueAsString(payload);
		} catch (JsonProcessingException ex) {
			throw new AppException(ErrorCode.INTERNAL_ERROR, "Failed to build outbox payload");
		}
	}
}
