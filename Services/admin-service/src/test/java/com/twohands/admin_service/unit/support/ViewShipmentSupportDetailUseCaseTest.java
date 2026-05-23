package com.twohands.admin_service.unit.support;

import com.twohands.admin_service.application.audit.AdminActionAuditLogger;
import com.twohands.admin_service.application.support.viewshipmentdetail.ViewShipmentSupportDetailQuery;
import com.twohands.admin_service.application.support.viewshipmentdetail.ViewShipmentSupportDetailResult;
import com.twohands.admin_service.application.support.viewshipmentdetail.ViewShipmentSupportDetailUseCase;
import com.twohands.admin_service.constant.AdminPermission;
import com.twohands.admin_service.domain.audit.AdminActionTargetType;
import com.twohands.admin_service.domain.auth.AdminAuthorizationService;
import com.twohands.admin_service.domain.integration.CommerceShipmentSupportGateway;
import com.twohands.admin_service.domain.support.ShipmentSupportDetail;
import com.twohands.admin_service.domain.support.ShipmentSupportShippingAddress;
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

class ViewShipmentSupportDetailUseCaseTest {

	private final AdminAuthorizationService adminAuthorizationService = mock(AdminAuthorizationService.class);
	private final CommerceShipmentSupportGateway commerceShipmentSupportGateway = mock(CommerceShipmentSupportGateway.class);
	private final AdminActionAuditLogger adminActionAuditLogger = mock(AdminActionAuditLogger.class);

	private ViewShipmentSupportDetailUseCase useCase;

	@BeforeEach
	void setUp() {
		useCase = new ViewShipmentSupportDetailUseCase(
				adminAuthorizationService,
				commerceShipmentSupportGateway,
				adminActionAuditLogger
		);
	}

	@Test
	void execute_returnsMaskedDetailAndLogsAudit() {
		UUID adminId = UUID.randomUUID();
		UUID shipmentId = UUID.randomUUID();
		when(adminAuthorizationService.requireCurrentAdminId()).thenReturn(adminId);
		when(commerceShipmentSupportGateway.isEnabled()).thenReturn(true);
		when(commerceShipmentSupportGateway.fetchShipmentSupportDetail(eq(shipmentId), eq("token")))
				.thenReturn(sampleDetail(shipmentId));

		ViewShipmentSupportDetailResult result = useCase.execute(new ViewShipmentSupportDetailQuery(shipmentId, "token"));

		assertEquals(shipmentId, result.detail().shipmentId());
		assertTrue(result.contactFieldsMasked());
		assertEquals("Nguyen ***", result.detail().shippingAddress().receiverName());

		verify(adminAuthorizationService).requirePermission(AdminPermission.SHIPMENT_SUPPORT_READ);
		verify(adminActionAuditLogger).logSuccess(
				eq(adminId),
				eq(AdminActionType.SHIPMENT_SUPPORT_VIEW.name()),
				eq(AdminActionTargetType.SHIPMENT),
				eq(shipmentId.toString()),
				any(),
				eq(Map.of("shipmentId", shipmentId.toString())),
				eq(Map.of(
						"internalStatus", "SHIPPED",
						"carrierStatus", "delivered",
						"contactFieldsMasked", true
				))
		);
	}

	@Test
	void execute_throwsWhenCommerceIntegrationDisabled() {
		when(adminAuthorizationService.requireCurrentAdminId()).thenReturn(UUID.randomUUID());
		when(commerceShipmentSupportGateway.isEnabled()).thenReturn(false);

		AppException ex = assertThrows(AppException.class, () -> useCase.execute(
				new ViewShipmentSupportDetailQuery(UUID.randomUUID(), "token")
		));

		assertEquals(ErrorCode.SERVICE_UNAVAILABLE, ex.getErrorCode());
	}

	private ShipmentSupportDetail sampleDetail(UUID shipmentId) {
		return new ShipmentSupportDetail(
				shipmentId,
				UUID.randomUUID(),
				UUID.randomUUID(),
				UUID.randomUUID(),
				"PROCESSING",
				"GHN",
				"STANDARD",
				"SHIPPED",
				"delivered",
				"GHN-123",
				"TRACK-9",
				BigDecimal.TEN,
				BigDecimal.ZERO,
				500,
				null,
				Instant.parse("2026-05-20T08:00:00Z"),
				null,
				Instant.parse("2026-05-19T00:00:00Z"),
				Instant.parse("2026-05-20T00:00:00Z"),
				new ShipmentSupportShippingAddress(
						"Nguyen Van A",
						"0901234567",
						"79",
						"760",
						"1",
						"detail",
						"full"
				),
				List.of(),
				List.of(),
				List.of()
		);
	}
}
