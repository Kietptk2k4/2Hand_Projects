package com.twohands.admin_service.unit.support;

import com.twohands.admin_service.application.audit.AdminActionAuditLogger;
import com.twohands.admin_service.application.support.vieworderdetail.ViewOrderSupportDetailQuery;
import com.twohands.admin_service.application.support.vieworderdetail.ViewOrderSupportDetailResult;
import com.twohands.admin_service.application.support.vieworderdetail.ViewOrderSupportDetailUseCase;
import com.twohands.admin_service.constant.AdminPermission;
import com.twohands.admin_service.domain.audit.AdminActionTargetType;
import com.twohands.admin_service.domain.auth.AdminAuthorizationService;
import com.twohands.admin_service.domain.integration.CommerceOrderSupportGateway;
import com.twohands.admin_service.domain.support.OrderSupportDetail;
import com.twohands.admin_service.domain.support.OrderSupportShippingAddress;
import com.twohands.admin_service.domain.support.OrderSupportShipment;
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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ViewOrderSupportDetailUseCaseTest {

	private final AdminAuthorizationService adminAuthorizationService = mock(AdminAuthorizationService.class);
	private final CommerceOrderSupportGateway commerceOrderSupportGateway = mock(CommerceOrderSupportGateway.class);
	private final AdminActionAuditLogger adminActionAuditLogger = mock(AdminActionAuditLogger.class);

	private ViewOrderSupportDetailUseCase useCase;

	@BeforeEach
	void setUp() {
		useCase = new ViewOrderSupportDetailUseCase(
				adminAuthorizationService,
				commerceOrderSupportGateway,
				adminActionAuditLogger
		);
	}

	@Test
	void execute_returnsMaskedDetailAndLogsAudit() {
		UUID adminId = UUID.randomUUID();
		UUID orderId = UUID.randomUUID();
		when(adminAuthorizationService.requireCurrentAdminId()).thenReturn(adminId);
		when(commerceOrderSupportGateway.isEnabled()).thenReturn(true);
		when(commerceOrderSupportGateway.fetchOrderSupportDetail(eq(orderId), eq("token")))
				.thenReturn(sampleDetail(orderId));

		ViewOrderSupportDetailResult result = useCase.execute(new ViewOrderSupportDetailQuery(orderId, "token"));

		assertEquals(orderId, result.detail().orderId());
		assertTrue(result.contactFieldsMasked());
		assertEquals("Nguyen ***", result.detail().shipments().getFirst().shippingAddress().receiverName());

		verify(adminAuthorizationService).requirePermission(AdminPermission.ORDER_SUPPORT_READ);
		verify(adminActionAuditLogger).logSuccess(
				eq(adminId),
				eq(AdminActionType.ORDER_SUPPORT_VIEW.name()),
				eq(AdminActionTargetType.ORDER),
				eq(orderId.toString()),
				any(),
				eq(Map.of("orderId", orderId.toString())),
				eq(Map.of("contactFieldsMasked", true))
		);
	}

	@Test
	void execute_throwsWhenCommerceIntegrationDisabled() {
		when(adminAuthorizationService.requireCurrentAdminId()).thenReturn(UUID.randomUUID());
		when(commerceOrderSupportGateway.isEnabled()).thenReturn(false);

		AppException ex = assertThrows(AppException.class, () -> useCase.execute(
				new ViewOrderSupportDetailQuery(UUID.randomUUID(), "token")
		));

		assertEquals(ErrorCode.SERVICE_UNAVAILABLE, ex.getErrorCode());
	}

	private OrderSupportDetail sampleDetail(UUID orderId) {
		OrderSupportShippingAddress address = new OrderSupportShippingAddress(
				"Nguyen Van A",
				"0901234567",
				"79",
				"760",
				"26734",
				"123 Street",
				"123 Street, District 1, HCMC"
		);
		return new OrderSupportDetail(
				orderId,
				UUID.randomUUID(),
				"SHIPPED",
				"PAID",
				"VNPAY",
				BigDecimal.TEN,
				BigDecimal.TEN,
				Instant.parse("2026-05-19T00:00:00Z"),
				Instant.parse("2026-05-20T00:00:00Z"),
				null,
				null,
				List.of(),
				List.of(new OrderSupportShipment(
						UUID.randomUUID(),
						UUID.randomUUID(),
						"SHIPPED",
						"GHN",
						"TRACK123",
						BigDecimal.ONE,
						"STANDARD",
						null,
						null,
						null,
						address,
						List.of()
				)),
				List.of()
		);
	}
}
