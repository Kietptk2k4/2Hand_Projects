package com.twohands.admin_service.domain.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.twohands.admin_service.exception.AppException;
import com.twohands.admin_service.exception.ErrorCode;

import java.math.BigDecimal;
import java.util.Set;
import java.util.regex.Pattern;

public final class SystemConfigPolicy {

	private static final Pattern CONFIG_KEY_PATTERN = Pattern.compile("^[A-Z][A-Z0-9_]{0,254}$");
	private static final Set<String> FORBIDDEN_KEY_FRAGMENTS = Set.of(
			"PASSWORD",
			"SECRET",
			"TOKEN",
			"API_KEY",
			"PRIVATE_KEY",
			"CREDENTIAL"
	);

	private SystemConfigPolicy() {
	}

	public static void validateCreateRequest(
			String configKey,
			String configValue,
			SystemConfigValueType valueType,
			String description,
			String reason
	) {
		validateConfigKey(configKey);
		assertNotSecretKey(configKey);
		validateReason(reason);
		validateDescription(description);
		validateConfigValue(configValue, valueType);
	}

	public static void validateUpdateRequest(
			String configValue,
			SystemConfigValueType valueType,
			String description,
			String reason
	) {
		validateReason(reason);
		validateDescription(description);
		validateConfigValue(configValue, valueType);
	}

	public static void validateToggleRequest(String reason) {
		validateReason(reason);
	}

	public static String formatActiveState(boolean active) {
		return Boolean.toString(active);
	}

	public static boolean isSecretLikeKey(String configKey) {
		if (configKey == null || configKey.isBlank()) {
			return false;
		}
		String upper = configKey.trim().toUpperCase();
		for (String fragment : FORBIDDEN_KEY_FRAGMENTS) {
			if (upper.contains(fragment)) {
				return true;
			}
		}
		return false;
	}

	public static String maskValueIfSecret(String configKey, String value) {
		if (value == null) {
			return null;
		}
		return isSecretLikeKey(configKey) ? "********" : value;
	}

	public static String normalizeConfigValue(String configValue, SystemConfigValueType valueType) {
		String trimmed = configValue.trim();
		if (valueType == SystemConfigValueType.BOOLEAN) {
			return trimmed.toLowerCase();
		}
		return trimmed;
	}

	public static SystemConfigValueType parseValueType(String rawValueType) {
		if (rawValueType == null || rawValueType.isBlank()) {
			throw validationError("value_type", "value_type is required");
		}
		try {
			return SystemConfigValueType.valueOf(rawValueType.trim().toUpperCase());
		} catch (IllegalArgumentException ex) {
			throw validationError("value_type", "value_type must be one of INTEGER, DECIMAL, STRING, BOOLEAN, JSON");
		}
	}

	private static void validateConfigKey(String configKey) {
		if (configKey == null || configKey.isBlank()) {
			throw validationError("config_key", "config_key is required");
		}
		String trimmed = configKey.trim();
		if (!CONFIG_KEY_PATTERN.matcher(trimmed).matches()) {
			throw validationError(
					"config_key",
					"config_key must be uppercase snake case starting with a letter (e.g. MAX_CART_ITEMS)"
			);
		}
	}

	private static void assertNotSecretKey(String configKey) {
		String upper = configKey.trim().toUpperCase();
		for (String fragment : FORBIDDEN_KEY_FRAGMENTS) {
			if (upper.contains(fragment)) {
				throw validationError("config_key", "config_key must not reference secrets or credentials");
			}
		}
	}

	private static void validateReason(String reason) {
		if (reason == null || reason.isBlank()) {
			throw validationError("reason", "reason is required");
		}
		if (reason.length() > 4000) {
			throw validationError("reason", "reason must be at most 4000 characters");
		}
	}

	private static void validateDescription(String description) {
		if (description != null && description.length() > 4000) {
			throw validationError("description", "description must be at most 4000 characters");
		}
	}

	private static void validateConfigValue(String configValue, SystemConfigValueType valueType) {
		if (configValue == null) {
			throw validationError("config_value", "config_value is required");
		}
		String trimmed = configValue.trim();
		if (trimmed.isEmpty() && valueType != SystemConfigValueType.STRING) {
			throw validationError("config_value", "config_value must not be blank");
		}

		switch (valueType) {
			case INTEGER -> {
				try {
					Integer.parseInt(trimmed);
				} catch (NumberFormatException ex) {
					throw validationError("config_value", "config_value must be a valid integer");
				}
			}
			case DECIMAL -> {
				try {
					new BigDecimal(trimmed);
				} catch (NumberFormatException ex) {
					throw validationError("config_value", "config_value must be a valid decimal number");
				}
			}
			case BOOLEAN -> {
				if (!"true".equalsIgnoreCase(trimmed) && !"false".equalsIgnoreCase(trimmed)) {
					throw validationError("config_value", "config_value must be true or false");
				}
			}
			case JSON -> {
				try {
					new ObjectMapper().readTree(trimmed);
				} catch (Exception ex) {
					throw validationError("config_value", "config_value must be valid JSON");
				}
			}
			case STRING -> {
				// any string value allowed
			}
		}
	}

	private static AppException validationError(String field, String reason) {
		return new AppException(ErrorCode.VALIDATION_ERROR, ErrorCode.VALIDATION_ERROR.defaultMessage(), field, reason);
	}
}
