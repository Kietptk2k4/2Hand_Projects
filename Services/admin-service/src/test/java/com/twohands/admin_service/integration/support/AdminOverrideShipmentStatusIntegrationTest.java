package com.twohands.admin_service.integration.support;

import com.twohands.admin_service.application.support.overrideshipmentstatus.AdminOverrideShipmentStatusCommand;
import com.twohands.admin_service.application.support.overrideshipmentstatus.AdminOverrideShipmentStatusUseCase;
import com.twohands.admin_service.domain.auth.AdminAuthorizationService;
import com.twohands.admin_service.domain.integration.CommerceShipmentSupportGateway;
import com.twohands.admin_service.domain.support.AdminOverrideShipmentStatusResult;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AdminOverrideShipmentStatusIntegrationTest {

	@Autowired
	private AdminOverrideShipmentStatusUseCase adminOverrideShipmentStatusUseCase;

	@MockBean
	private AdminAuthorizationService adminAuthorizationService;

	@MockBean
	private CommerceShipmentSupportGateway commerceShipmentSupportGateway;

	@Test
	void execute_returnsOverrideResultFromCommerceGateway() {
		UUID shipmentId = UUID.randomUUID();
		UUID orderId = UUID.randomUUID();
		when(adminAuthorizationService.requireCurrentAdminId()).thenReturn(UUID.randomUUID());
		when(commerceShipmentSupportGateway.isEnabled()).thenReturn(true);
		when(commerceShipmentSupportGateway.overrideShipmentStatus(
				shipmentId,
				"DELIVERED",
				"GHN webhook khong ve sau 48h",
				false,
				"bearer"
		)).thenReturn(new AdminOverrideShipmentStatusResult(
				shipmentId,
				orderId,
				"GHN",
				"SHIPPED",
				"DELIVERED",
				"ADMIN",
				"admin_override",
				1,
				Instant.parse("2026-06-10T10:00:00Z")
		));

		var result = adminOverrideShipmentStatusUseCase.execute(new AdminOverrideShipmentStatusCommand(
				shipmentId,
				"DELIVERED",
				"GHN webhook khong ve sau 48h",
				false,
				"bearer"
		));

		assertEquals(shipmentId, result.shipmentId());
		assertEquals(orderId, result.orderId());
		assertEquals("DELIVERED", result.currentStatus());
		assertEquals("admin_override", result.rawStatus());
	}
}
