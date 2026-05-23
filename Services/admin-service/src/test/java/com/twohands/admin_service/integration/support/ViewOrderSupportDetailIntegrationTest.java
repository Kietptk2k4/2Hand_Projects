package com.twohands.admin_service.integration.support;

import com.twohands.admin_service.application.support.vieworderdetail.ViewOrderSupportDetailQuery;
import com.twohands.admin_service.application.support.vieworderdetail.ViewOrderSupportDetailUseCase;
import com.twohands.admin_service.domain.auth.AdminAuthorizationService;
import com.twohands.admin_service.domain.integration.CommerceOrderSupportGateway;
import com.twohands.admin_service.domain.support.OrderSupportDetail;
import com.twohands.admin_service.domain.support.OrderSupportShippingAddress;
import com.twohands.admin_service.domain.support.OrderSupportShipment;
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
class ViewOrderSupportDetailIntegrationTest {

	@Autowired
	private ViewOrderSupportDetailUseCase viewOrderSupportDetailUseCase;

	@MockBean
	private AdminAuthorizationService adminAuthorizationService;

	@MockBean
	private CommerceOrderSupportGateway commerceOrderSupportGateway;

	@Test
	void execute_returnsMaskedOrderSupportDetail() {
		UUID adminId = UUID.randomUUID();
		UUID orderId = UUID.randomUUID();
		when(adminAuthorizationService.requireCurrentAdminId()).thenReturn(adminId);
		when(commerceOrderSupportGateway.isEnabled()).thenReturn(true);
		when(commerceOrderSupportGateway.fetchOrderSupportDetail(orderId, "bearer"))
				.thenReturn(buildDetail(orderId));

		var result = viewOrderSupportDetailUseCase.execute(new ViewOrderSupportDetailQuery(orderId, "bearer"));

		assertEquals(orderId, result.detail().orderId());
		assertTrue(result.contactFieldsMasked());
		assertEquals("***4567", result.detail().shipments().getFirst().shippingAddress().phone());
	}

	private OrderSupportDetail buildDetail(UUID orderId) {
		OrderSupportShippingAddress address = new OrderSupportShippingAddress(
				"Tran Thi B",
				"0901234567",
				"01",
				"001",
				"00001",
				"Detail",
				"Full"
		);
		return new OrderSupportDetail(
				orderId,
				UUID.randomUUID(),
				"CONFIRMED",
				"UNPAID",
				"COD",
				BigDecimal.valueOf(200),
				BigDecimal.valueOf(200),
				Instant.parse("2026-05-18T10:00:00Z"),
				Instant.parse("2026-05-18T11:00:00Z"),
				null,
				null,
				List.of(),
				List.of(new OrderSupportShipment(
						UUID.randomUUID(),
						UUID.randomUUID(),
						"PENDING",
						null,
						null,
						BigDecimal.ZERO,
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
