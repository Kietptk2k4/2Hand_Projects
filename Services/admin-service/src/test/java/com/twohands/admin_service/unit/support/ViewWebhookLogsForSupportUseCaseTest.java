package com.twohands.admin_service.unit.support;

import com.twohands.admin_service.application.audit.AdminActionAuditLogger;
import com.twohands.admin_service.application.support.viewwebhooklogs.ViewWebhookLogsForSupportQuery;
import com.twohands.admin_service.application.support.viewwebhooklogs.ViewWebhookLogsForSupportResult;
import com.twohands.admin_service.application.support.viewwebhooklogs.ViewWebhookLogsForSupportUseCase;
import com.twohands.admin_service.constant.AdminPermission;
import com.twohands.admin_service.domain.audit.AdminActionTargetType;
import com.twohands.admin_service.domain.auth.AdminAuthorizationService;
import com.twohands.admin_service.domain.common.PagedResult;
import com.twohands.admin_service.domain.integration.CommerceWebhookSupportGateway;
import com.twohands.admin_service.domain.support.WebhookSupportLogEntry;
import com.twohands.admin_service.exception.AppException;
import com.twohands.admin_service.exception.ErrorCode;
import com.twohands.admin_service.infrastructure.persistence.jpa.enums.AdminActionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ViewWebhookLogsForSupportUseCaseTest {

	private final AdminAuthorizationService adminAuthorizationService = mock(AdminAuthorizationService.class);
	private final CommerceWebhookSupportGateway commerceWebhookSupportGateway = mock(CommerceWebhookSupportGateway.class);
	private final AdminActionAuditLogger adminActionAuditLogger = mock(AdminActionAuditLogger.class);

	private ViewWebhookLogsForSupportUseCase useCase;

	@BeforeEach
	void setUp() {
		useCase = new ViewWebhookLogsForSupportUseCase(
				adminAuthorizationService,
				commerceWebhookSupportGateway,
				adminActionAuditLogger
		);
	}

	@Test
	void execute_returnsPagedLogsAndLogsAudit() {
		UUID adminId = UUID.randomUUID();
		WebhookSupportLogEntry entry = new WebhookSupportLogEntry(
				UUID.randomUUID(),
				"PAYOS",
				"PAYOS-1",
				"PAYMENT_SUCCESS",
				"PROCESSED",
				true,
				0,
				"PAYOS:PAYOS-1:PAYMENT_SUCCESS",
				Map.of("code", "00"),
				Instant.parse("2026-05-20T10:00:00Z")
		);
		when(adminAuthorizationService.requireCurrentAdminId()).thenReturn(adminId);
		when(commerceWebhookSupportGateway.isEnabled()).thenReturn(true);
		when(commerceWebhookSupportGateway.searchWebhookLogs(
				eq("PAYOS"),
				isNull(),
				isNull(),
				isNull(),
				isNull(),
				eq(1),
				eq(20),
				eq("token")
		)).thenReturn(new PagedResult<>(List.of(entry), 1, 20, 1L, 1));

		ViewWebhookLogsForSupportResult result = useCase.execute(new ViewWebhookLogsForSupportQuery(
				"PAYOS",
				null,
				null,
				null,
				null,
				1,
				20,
				"token"
		));

		assertEquals(1, result.totalElements());
		assertEquals("PAYOS", result.logs().getFirst().provider());

		verify(adminAuthorizationService).requirePermission(AdminPermission.WEBHOOK_SUPPORT_READ);
		verify(adminActionAuditLogger).logSuccess(
				eq(adminId),
				eq(AdminActionType.WEBHOOK_SUPPORT_VIEW.name()),
				eq(AdminActionTargetType.WEBHOOK),
				eq("search"),
				any(),
				eq(Map.of("provider", "PAYOS", "page", 1, "size", 20)),
				eq(Map.of("totalElements", 1L))
		);
	}

	@Test
	void execute_throwsWhenCommerceIntegrationDisabled() {
		when(adminAuthorizationService.requireCurrentAdminId()).thenReturn(UUID.randomUUID());
		when(commerceWebhookSupportGateway.isEnabled()).thenReturn(false);

		AppException ex = assertThrows(AppException.class, () -> useCase.execute(
				new ViewWebhookLogsForSupportQuery(null, null, null, null, null, 1, 20, "token")
		));

		assertEquals(ErrorCode.SERVICE_UNAVAILABLE, ex.getErrorCode());
	}
}
