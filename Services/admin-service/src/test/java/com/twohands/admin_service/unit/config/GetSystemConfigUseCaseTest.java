package com.twohands.admin_service.unit.config;

import com.twohands.admin_service.application.config.getsystemconfig.GetSystemConfigQuery;
import com.twohands.admin_service.application.config.getsystemconfig.GetSystemConfigUseCase;
import com.twohands.admin_service.constant.AdminPermission;
import com.twohands.admin_service.domain.auth.AdminAuthorizationService;
import com.twohands.admin_service.domain.config.SystemConfig;
import com.twohands.admin_service.domain.config.SystemConfigRepository;
import com.twohands.admin_service.domain.config.SystemConfigValueType;
import com.twohands.admin_service.exception.AppException;
import com.twohands.admin_service.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class GetSystemConfigUseCaseTest {

	private final AdminAuthorizationService adminAuthorizationService = mock(AdminAuthorizationService.class);
	private final SystemConfigRepository systemConfigRepository = mock(SystemConfigRepository.class);

	private GetSystemConfigUseCase useCase;

	@BeforeEach
	void setUp() {
		useCase = new GetSystemConfigUseCase(
				adminAuthorizationService,
				systemConfigRepository
		);
	}

	@Test
	void shouldReturnConfigDetail() {
		UUID configId = UUID.randomUUID();
		UUID adminId = UUID.randomUUID();
		Instant now = Instant.now();

		SystemConfig config = new SystemConfig(
				configId,
				"MAX_CART_ITEMS",
				"100",
				SystemConfigValueType.INTEGER,
				"Cart limit",
				true,
				adminId,
				now,
				adminId,
				now
		);

		when(adminAuthorizationService.requireCurrentAdminId()).thenReturn(adminId);
		when(systemConfigRepository.findById(configId)).thenReturn(Optional.of(config));

		var result = useCase.execute(new GetSystemConfigQuery(configId));

		assertThat(result.configId()).isEqualTo(configId);
		assertThat(result.configKey()).isEqualTo("MAX_CART_ITEMS");
		assertThat(result.configValue()).isEqualTo("100");
		assertThat(result.valueMasked()).isFalse();
		verify(adminAuthorizationService).requirePermission(AdminPermission.SYSTEM_CONFIG_VIEW);
	}

	@Test
	void shouldMaskValueForSecretLikeConfigKey() {
		UUID configId = UUID.randomUUID();
		Instant now = Instant.now();

		SystemConfig config = new SystemConfig(
				configId,
				"API_KEY_COMMERCE",
				"secret-value",
				SystemConfigValueType.STRING,
				null,
				true,
				UUID.randomUUID(),
				now,
				UUID.randomUUID(),
				now
		);

		when(adminAuthorizationService.requireCurrentAdminId()).thenReturn(UUID.randomUUID());
		when(systemConfigRepository.findById(configId)).thenReturn(Optional.of(config));

		var result = useCase.execute(new GetSystemConfigQuery(configId));

		assertThat(result.valueMasked()).isTrue();
		assertThat(result.configValue()).isEqualTo("********");
	}

	@Test
	void shouldReturnNotFoundWhenConfigMissing() {
		UUID configId = UUID.randomUUID();
		when(adminAuthorizationService.requireCurrentAdminId()).thenReturn(UUID.randomUUID());
		when(systemConfigRepository.findById(configId)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> useCase.execute(new GetSystemConfigQuery(configId)))
				.isInstanceOf(AppException.class)
				.extracting(ex -> ((AppException) ex).getErrorCode())
				.isEqualTo(ErrorCode.RESOURCE_NOT_FOUND);
	}
}
