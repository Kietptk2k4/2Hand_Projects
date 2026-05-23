package com.twohands.admin_service.unit.config;

import com.twohands.admin_service.application.config.viewhistory.ViewSystemConfigHistoryQuery;
import com.twohands.admin_service.application.config.viewhistory.ViewSystemConfigHistoryUseCase;
import com.twohands.admin_service.constant.AdminPermission;
import com.twohands.admin_service.domain.auth.AdminAuthorizationService;
import com.twohands.admin_service.domain.common.PageRequest;
import com.twohands.admin_service.domain.common.PagedResult;
import com.twohands.admin_service.domain.config.SystemConfig;
import com.twohands.admin_service.domain.config.SystemConfigHistory;
import com.twohands.admin_service.domain.config.SystemConfigHistoryRepository;
import com.twohands.admin_service.domain.config.SystemConfigRepository;
import com.twohands.admin_service.domain.config.SystemConfigValueType;
import com.twohands.admin_service.exception.AppException;
import com.twohands.admin_service.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ViewSystemConfigHistoryUseCaseTest {

	private final AdminAuthorizationService adminAuthorizationService = mock(AdminAuthorizationService.class);
	private final SystemConfigRepository systemConfigRepository = mock(SystemConfigRepository.class);
	private final SystemConfigHistoryRepository systemConfigHistoryRepository = mock(SystemConfigHistoryRepository.class);

	private ViewSystemConfigHistoryUseCase useCase;

	@BeforeEach
	void setUp() {
		useCase = new ViewSystemConfigHistoryUseCase(
				adminAuthorizationService,
				systemConfigRepository,
				systemConfigHistoryRepository
		);
	}

	@Test
	void shouldReturnPagedHistoryNewestFirst() {
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

		SystemConfigHistory entry = new SystemConfigHistory(
				UUID.randomUUID(),
				"MAX_CART_ITEMS",
				"50",
				"100",
				adminId,
				"Increase limit",
				now
		);

		when(adminAuthorizationService.requireCurrentAdminId()).thenReturn(adminId);
		when(systemConfigRepository.findById(configId)).thenReturn(Optional.of(config));
		when(systemConfigHistoryRepository.findByConfigKeyOrderByCreatedAtDesc(
				"MAX_CART_ITEMS",
				new PageRequest(1, 20)
		)).thenReturn(new PagedResult<>(List.of(entry), 1, 20, 1, 1));

		var result = useCase.execute(new ViewSystemConfigHistoryQuery(configId, 1, 20));

		assertThat(result.configKey()).isEqualTo("MAX_CART_ITEMS");
		assertThat(result.history()).hasSize(1);
		assertThat(result.history().get(0).oldValue()).isEqualTo("50");
		assertThat(result.history().get(0).newValue()).isEqualTo("100");
		assertThat(result.valuesMasked()).isFalse();
		verify(adminAuthorizationService).requirePermission(AdminPermission.SYSTEM_CONFIG_VIEW);
	}

	@Test
	void shouldMaskValuesForSecretLikeConfigKey() {
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

		SystemConfigHistory entry = new SystemConfigHistory(
				UUID.randomUUID(),
				"API_KEY_COMMERCE",
				"old-secret",
				"new-secret",
				UUID.randomUUID(),
				"Rotation",
				now
		);

		when(adminAuthorizationService.requireCurrentAdminId()).thenReturn(UUID.randomUUID());
		when(systemConfigRepository.findById(configId)).thenReturn(Optional.of(config));
		when(systemConfigHistoryRepository.findByConfigKeyOrderByCreatedAtDesc(
				"API_KEY_COMMERCE",
				new PageRequest(1, 20)
		)).thenReturn(new PagedResult<>(List.of(entry), 1, 20, 1, 1));

		var result = useCase.execute(new ViewSystemConfigHistoryQuery(configId, 1, 20));

		assertThat(result.valuesMasked()).isTrue();
		assertThat(result.history().get(0).oldValue()).isEqualTo("********");
		assertThat(result.history().get(0).newValue()).isEqualTo("********");
	}

	@Test
	void shouldReturnNotFoundWhenConfigMissing() {
		UUID configId = UUID.randomUUID();
		when(adminAuthorizationService.requireCurrentAdminId()).thenReturn(UUID.randomUUID());
		when(systemConfigRepository.findById(configId)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> useCase.execute(new ViewSystemConfigHistoryQuery(configId, 1, 20)))
				.isInstanceOf(AppException.class)
				.extracting(ex -> ((AppException) ex).getErrorCode())
				.isEqualTo(ErrorCode.RESOURCE_NOT_FOUND);
	}
}
