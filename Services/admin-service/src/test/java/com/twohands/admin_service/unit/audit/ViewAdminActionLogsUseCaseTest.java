package com.twohands.admin_service.unit.audit;

import com.twohands.admin_service.application.audit.AdminActionLogResponseMapper;
import com.twohands.admin_service.application.audit.JacksonAuditPayloadSanitizer;
import com.twohands.admin_service.application.audit.viewlogs.ViewAdminActionLogsQuery;
import com.twohands.admin_service.application.audit.viewlogs.ViewAdminActionLogsUseCase;
import com.twohands.admin_service.constant.AdminPermission;
import com.twohands.admin_service.domain.audit.AdminActionLog;
import com.twohands.admin_service.domain.audit.AdminActionLogRepository;
import com.twohands.admin_service.domain.audit.AdminActionStatus;
import com.twohands.admin_service.domain.auth.AdminAuthorizationService;
import com.twohands.admin_service.domain.common.PageRequest;
import com.twohands.admin_service.domain.common.PagedResult;
import com.twohands.admin_service.exception.AppException;
import com.twohands.admin_service.exception.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ViewAdminActionLogsUseCaseTest {

	private final AdminAuthorizationService adminAuthorizationService = mock(AdminAuthorizationService.class);
	private final AdminActionLogRepository adminActionLogRepository = mock(AdminActionLogRepository.class);

	private ViewAdminActionLogsUseCase useCase;

	@BeforeEach
	void setUp() {
		useCase = new ViewAdminActionLogsUseCase(
				adminAuthorizationService,
				adminActionLogRepository,
				new AdminActionLogResponseMapper(new JacksonAuditPayloadSanitizer(new ObjectMapper()))
		);
	}

	@Test
	void shouldReturnPagedLogs() {
		UUID adminId = UUID.randomUUID();
		UUID logId = UUID.randomUUID();
		Instant now = Instant.now();

		when(adminAuthorizationService.requireCurrentAdminId()).thenReturn(adminId);
		when(adminActionLogRepository.search(any(), any(PageRequest.class))).thenReturn(new PagedResult<>(
				List.of(new AdminActionLog(
						logId,
						adminId,
						"USER_SUSPEND",
						"USER",
						"user-1",
						AdminActionStatus.SUCCESS,
						null,
						"{\"status\":\"SUCCESS\"}",
						"127.0.0.1",
						"agent",
						now
				)),
				1,
				20,
				1,
				1
		));

		var result = useCase.execute(new ViewAdminActionLogsQuery(
				adminId,
				"USER_SUSPEND",
				"USER",
				"user-1",
				"SUCCESS",
				null,
				null,
				1,
				20
		));

		assertThat(result.logs()).hasSize(1);
		assertThat(result.logs().get(0).logId()).isEqualTo(logId);
		verify(adminAuthorizationService).requirePermission(AdminPermission.ADMIN_AUDIT_VIEW);
	}

	@Test
	void shouldRejectInvalidDateRange() {
		when(adminAuthorizationService.requireCurrentAdminId()).thenReturn(UUID.randomUUID());

		assertThatThrownBy(() -> useCase.execute(new ViewAdminActionLogsQuery(
				null,
				null,
				null,
				null,
				null,
				"2026-05-23T12:00:00Z",
				"2026-05-22T12:00:00Z",
				1,
				20
		)))
				.isInstanceOf(AppException.class)
				.extracting(ex -> ((AppException) ex).getErrorCode())
				.isEqualTo(ErrorCode.VALIDATION_ERROR);
	}
}
