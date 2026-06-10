package com.twohands.admin_service.unit.support;

import com.twohands.admin_service.application.audit.AdminActionAuditLogger;
import com.twohands.admin_service.application.support.viewpaymentsfor.ViewPaymentsForSupportQuery;
import com.twohands.admin_service.application.support.viewpaymentsfor.ViewPaymentsForSupportResult;
import com.twohands.admin_service.application.support.viewpaymentsfor.ViewPaymentsForSupportUseCase;
import com.twohands.admin_service.constant.AdminPermission;
import com.twohands.admin_service.domain.audit.AdminActionTargetType;
import com.twohands.admin_service.domain.auth.AdminAuthorizationService;
import com.twohands.admin_service.domain.common.PagedResult;
import com.twohands.admin_service.domain.integration.CommercePaymentSupportGateway;
import com.twohands.admin_service.domain.support.PaymentSupportListEntry;
import com.twohands.admin_service.infrastructure.persistence.jpa.enums.AdminActionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ViewPaymentsForSupportUseCaseTest {

	private final AdminAuthorizationService adminAuthorizationService = mock(AdminAuthorizationService.class);
	private final CommercePaymentSupportGateway commercePaymentSupportGateway = mock(CommercePaymentSupportGateway.class);
	private final AdminActionAuditLogger adminActionAuditLogger = mock(AdminActionAuditLogger.class);

	private ViewPaymentsForSupportUseCase useCase;

	@BeforeEach
	void setUp() {
		useCase = new ViewPaymentsForSupportUseCase(
				adminAuthorizationService,
				commercePaymentSupportGateway,
				adminActionAuditLogger
		);
	}

	@Test
	void execute_returnsPagedPaymentsAndLogsAudit() {
		UUID adminId = UUID.randomUUID();
		UUID paymentId = UUID.randomUUID();
		PaymentSupportListEntry entry = new PaymentSupportListEntry(
				paymentId,
				UUID.randomUUID(),
				"COD",
				BigDecimal.valueOf(200000),
				"VND",
				"PENDING",
				null,
				Instant.parse("2026-06-09T10:00:00Z")
		);

		when(adminAuthorizationService.requireCurrentAdminId()).thenReturn(adminId);
		when(commercePaymentSupportGateway.isEnabled()).thenReturn(true);
		when(commercePaymentSupportGateway.searchPayments(
				eq(null),
				eq("COD"),
				eq(null),
				eq(null),
				eq(null),
				eq(1),
				eq(20),
				eq("token")
		)).thenReturn(new PagedResult<>(List.of(entry), 1, 20, 1L, 1));

		ViewPaymentsForSupportResult result = useCase.execute(new ViewPaymentsForSupportQuery(
				null,
				"COD",
				null,
				null,
				null,
				1,
				20,
				"token"
		));

		assertEquals(1, result.payments().size());
		assertEquals("COD", result.payments().getFirst().paymentMethod());

		verify(adminAuthorizationService).requirePermission(AdminPermission.PAYMENT_SUPPORT_READ);
		verify(adminActionAuditLogger).logSuccess(
				eq(adminId),
				eq(AdminActionType.PAYMENT_SUPPORT_VIEW.name()),
				eq(AdminActionTargetType.PAYMENT),
				eq("search"),
				any(),
				any(),
				eq(Map.of("totalElements", 1L))
		);
	}
}
