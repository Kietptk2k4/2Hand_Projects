package com.twohands.admin_service.unit.support;

import com.twohands.admin_service.application.audit.AdminActionAuditLogger;
import com.twohands.admin_service.application.support.viewordersfor.ViewOrdersForSupportQuery;
import com.twohands.admin_service.application.support.viewordersfor.ViewOrdersForSupportResult;
import com.twohands.admin_service.application.support.viewordersfor.ViewOrdersForSupportUseCase;
import com.twohands.admin_service.constant.AdminPermission;
import com.twohands.admin_service.domain.audit.AdminActionTargetType;
import com.twohands.admin_service.domain.auth.AdminAuthorizationService;
import com.twohands.admin_service.domain.common.PagedResult;
import com.twohands.admin_service.domain.integration.CommerceOrderSupportGateway;
import com.twohands.admin_service.domain.support.OrderSupportListEntry;
import com.twohands.admin_service.exception.AppException;
import com.twohands.admin_service.exception.ErrorCode;
import com.twohands.admin_service.infrastructure.persistence.jpa.enums.AdminActionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ViewOrdersForSupportUseCaseTest {

	private final AdminAuthorizationService adminAuthorizationService = mock(AdminAuthorizationService.class);
	private final CommerceOrderSupportGateway commerceOrderSupportGateway = mock(CommerceOrderSupportGateway.class);
	private final AdminActionAuditLogger adminActionAuditLogger = mock(AdminActionAuditLogger.class);

	private ViewOrdersForSupportUseCase useCase;

	@BeforeEach
	void setUp() {
		useCase = new ViewOrdersForSupportUseCase(
				adminAuthorizationService,
				commerceOrderSupportGateway,
				adminActionAuditLogger
		);
	}

	@Test
	void execute_returnsPagedOrdersAndLogsAudit() {
		UUID adminId = UUID.randomUUID();
		OrderSupportListEntry entry = new OrderSupportListEntry(
				UUID.randomUUID(),
				UUID.randomUUID(),
				"PROCESSING",
				"PAID",
				"PAYOS",
				new BigDecimal("350000"),
				Instant.parse("2026-05-19T10:00:00Z"),
				Instant.parse("2026-05-20T08:00:00Z")
		);
		when(adminAuthorizationService.requireCurrentAdminId()).thenReturn(adminId);
		when(commerceOrderSupportGateway.isEnabled()).thenReturn(true);
		when(commerceOrderSupportGateway.searchOrders(
				eq("PROCESSING"),
				eq("PAYOS"),
				eq(null),
				eq(null),
				eq("created_at"),
				eq(1),
				eq(20),
				eq("token")
		)).thenReturn(new PagedResult<>(List.of(entry), 1, 20, 1L, 1));

		ViewOrdersForSupportResult result = useCase.execute(new ViewOrdersForSupportQuery(
				"PROCESSING",
				"PAYOS",
				null,
				null,
				"created_at",
				1,
				20,
				"token"
		));

		assertEquals(1, result.totalElements());
		assertEquals("PAYOS", result.orders().getFirst().paymentMethod());

		verify(adminAuthorizationService).requirePermission(AdminPermission.ORDER_SUPPORT_READ);
		verify(adminActionAuditLogger).logSuccess(
				eq(adminId),
				eq(AdminActionType.ORDER_SUPPORT_VIEW.name()),
				eq(AdminActionTargetType.ORDER),
				eq("search"),
				any(),
				eq(Map.of("status", "PROCESSING", "paymentMethod", "PAYOS", "sort", "created_at", "page", 1, "size", 20)),
				eq(Map.of("totalElements", 1L))
		);
	}

	@Test
	void execute_throwsWhenCommerceIntegrationDisabled() {
		when(adminAuthorizationService.requireCurrentAdminId()).thenReturn(UUID.randomUUID());
		when(commerceOrderSupportGateway.isEnabled()).thenReturn(false);

		AppException ex = assertThrows(AppException.class, () -> useCase.execute(
				new ViewOrdersForSupportQuery(null, null, null, null, null, 1, 20, "token")
		));

		assertEquals(ErrorCode.SERVICE_UNAVAILABLE, ex.getErrorCode());
	}
}
