package com.twohands.admin_service.unit.config;

import com.twohands.admin_service.domain.config.SystemConfigPolicy;
import com.twohands.admin_service.domain.config.SystemConfigValueType;
import com.twohands.admin_service.exception.AppException;
import com.twohands.admin_service.exception.ErrorCode;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SystemConfigPolicyTest {

	@Test
	void shouldAcceptValidIntegerConfig() {
		SystemConfigPolicy.validateCreateRequest(
				"MAX_CART_ITEMS",
				"50",
				SystemConfigValueType.INTEGER,
				"Cart limit",
				"Initial rollout"
		);
	}

	@Test
	void shouldRejectSecretKey() {
		assertThatThrownBy(() -> SystemConfigPolicy.validateCreateRequest(
				"API_KEY_COMMERCE",
				"value",
				SystemConfigValueType.STRING,
				null,
				"reason"
		))
				.isInstanceOf(AppException.class)
				.extracting(ex -> ((AppException) ex).getErrorCode())
				.isEqualTo(ErrorCode.VALIDATION_ERROR);
	}

	@Test
	void shouldRejectInvalidIntegerValue() {
		assertThatThrownBy(() -> SystemConfigPolicy.validateCreateRequest(
				"MAX_CART_ITEMS",
				"not-a-number",
				SystemConfigValueType.INTEGER,
				null,
				"reason"
		))
				.isInstanceOf(AppException.class);
	}

	@Test
	void shouldParseValueType() {
		assertThat(SystemConfigPolicy.parseValueType("json")).isEqualTo(SystemConfigValueType.JSON);
	}
}
