package com.twohands.admin_service.unit.support;

import com.twohands.admin_service.application.audit.AdminActionAuditLogger;
import com.twohands.admin_service.application.support.viewpaymentdetail.ViewPaymentSupportDetailQuery;
import com.twohands.admin_service.application.support.viewpaymentdetail.ViewPaymentSupportDetailResult;
import com.twohands.admin_service.application.support.viewpaymentdetail.ViewPaymentSupportDetailUseCase;
import com.twohands.admin_service.constant.AdminPermission;
import com.twohands.admin_service.domain.audit.AdminActionTargetType;
import com.twohands.admin_service.domain.auth.AdminAuthorizationService;
import com.twohands.admin_service.domain.integration.CommercePaymentSupportGateway;
import com.twohands.admin_service.domain.support.PaymentSupportDetail;
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

class ViewPaymentSupportDetailUseCaseTest {

	private final AdminAuthorizationService adminAuthorizationService = mock(AdminAuthorizationService.class);
	private final CommercePaymentSupportGateway commercePaymentSupportGateway = mock(CommercePaymentSupportGateway.class);
	private final AdminActionAuditLogger adminActionAuditLogger = mock(AdminActionAuditLogger.class);

	private ViewPaymentSupportDetailUseCase useCase;

	@BeforeEach
	void setUp() {
		useCase = new ViewPaymentSupportDetailUseCase(
				adminAuthorizationService,
				commercePaymentSupportGateway,
				adminActionAuditLogger
		);
	}

	@Test
	void execute_returnsDetailAndLogsAudit() {
		UUID adminId = UUID.randomUUID();
		UUID paymentId = UUID.randomUUID();
		PaymentSupportDetail detail = sampleDetail(paymentId);

		when(adminAuthorizationService.requireCurrentAdminId()).thenReturn(adminId);
		when(commercePaymentSupportGateway.isEnabled()).thenReturn(true);
		when(commercePaymentSupportGateway.fetchPaymentSupportDetail(eq(paymentId), eq("token"))).thenReturn(detail);

		ViewPaymentSupportDetailResult result = useCase.execute(new ViewPaymentSupportDetailQuery(paymentId, "token"));

		assertEquals(paymentId, result.detail().paymentId());
		assertEquals("RECONCILED", result.detail().reconciliationStatus());

		verify(adminAuthorizationService).requirePermission(AdminPermission.PAYMENT_SUPPORT_READ);
		verify(adminActionAuditLogger).logSuccess(
				eq(adminId),
				eq(AdminActionType.PAYMENT_SUPPORT_VIEW.name()),
				eq(AdminActionTargetType.PAYMENT),
				eq(paymentId.toString()),
				any(),
				eq(Map.of("paymentId", paymentId.toString())),
				eq(Map.of("reconciliationStatus", "RECONCILED"))
		);
	}

	@Test
	void execute_throwsWhenCommerceIntegrationDisabled() {
		when(adminAuthorizationService.requireCurrentAdminId()).thenReturn(UUID.randomUUID());
		when(commercePaymentSupportGateway.isEnabled()).thenReturn(false);

		AppException ex = assertThrows(AppException.class, () -> useCase.execute(
				new ViewPaymentSupportDetailQuery(UUID.randomUUID(), "token")
		));

		assertEquals(ErrorCode.SERVICE_UNAVAILABLE, ex.getErrorCode());
	}

	private PaymentSupportDetail sampleDetail(UUID paymentId) {
		return new PaymentSupportDetail(
				paymentId,
				UUID.randomUUID(),
				UUID.randomUUID(),
				"PAYOS",
				BigDecimal.valueOf(150000),
				"VND",
				"PAID",
				Instant.parse("2026-05-20T08:00:00Z"),
				null,
				Instant.parse("2026-05-19T10:00:00Z"),
				Instant.parse("2026-05-20T08:00:00Z"),
				"PAYOS-123",
				"TX-999",
				false,
				null,
				"PROCESSING",
				"PAID",
				"RECONCILED",
				List.of(),
				List.of()
		);
	}
}
