package com.twohands.admin_service.integration.support;

import com.twohands.admin_service.application.support.viewshipmentdetail.ViewShipmentSupportDetailQuery;
import com.twohands.admin_service.application.support.viewshipmentdetail.ViewShipmentSupportDetailUseCase;
import com.twohands.admin_service.domain.auth.AdminAuthorizationService;
import com.twohands.admin_service.domain.integration.CommerceShipmentSupportGateway;
import com.twohands.admin_service.domain.support.ShipmentSupportDetail;
import com.twohands.admin_service.domain.support.ShipmentSupportShippingAddress;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ViewShipmentSupportDetailIntegrationTest {

	@Autowired
	private ViewShipmentSupportDetailUseCase viewShipmentSupportDetailUseCase;

	@MockBean
	private AdminAuthorizationService adminAuthorizationService;

	@MockBean
	private CommerceShipmentSupportGateway commerceShipmentSupportGateway;

	@Test
	void execute_returnsMaskedShipmentSupportDetail() {
		UUID shipmentId = UUID.randomUUID();
		when(adminAuthorizationService.requireCurrentAdminId()).thenReturn(UUID.randomUUID());
		when(commerceShipmentSupportGateway.isEnabled()).thenReturn(true);
		when(commerceShipmentSupportGateway.fetchShipmentSupportDetail(shipmentId, "bearer"))
				.thenReturn(buildDetail(shipmentId));

		var result = viewShipmentSupportDetailUseCase.execute(new ViewShipmentSupportDetailQuery(shipmentId, "bearer"));

		assertEquals(shipmentId, result.detail().shipmentId());
		assertTrue(result.contactFieldsMasked());
		assertEquals("***4567", result.detail().shippingAddress().phone());
	}

	private ShipmentSupportDetail buildDetail(UUID shipmentId) {
		return new ShipmentSupportDetail(
				shipmentId,
				UUID.randomUUID(),
				UUID.randomUUID(),
				UUID.randomUUID(),
				"PROCESSING",
				"GHN",
				"STANDARD",
				"SHIPPED",
				"transporting",
				"GHN-777",
				"TRACK-77",
				BigDecimal.valueOf(30000),
				BigDecimal.ZERO,
				800,
				null,
				Instant.parse("2026-05-20T10:00:00Z"),
				null,
				Instant.parse("2026-05-19T09:00:00Z"),
				Instant.parse("2026-05-20T10:00:00Z"),
				new ShipmentSupportShippingAddress(
						"Le Thi C",
						"0901234567",
						"01",
						"001",
						"00001",
						"Detail",
						"Full"
				),
				List.of(),
				List.of(),
				List.of()
		);
	}
}
