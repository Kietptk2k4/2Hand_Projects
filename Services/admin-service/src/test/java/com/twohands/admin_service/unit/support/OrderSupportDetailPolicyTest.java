package com.twohands.admin_service.unit.support;

import com.twohands.admin_service.domain.support.OrderSupportDetail;
import com.twohands.admin_service.domain.support.OrderSupportDetailPolicy;
import com.twohands.admin_service.domain.support.OrderSupportShipment;
import com.twohands.admin_service.domain.support.OrderSupportShippingAddress;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OrderSupportDetailPolicyTest {

	@Test
	void maskContactFields_masksShippingAddressWhenPiiNotRevealed() {
		UUID orderId = UUID.randomUUID();
		UUID buyerId = UUID.randomUUID();
		OrderSupportShippingAddress address = new OrderSupportShippingAddress(
				"Nguyen Van A",
				"0901234567",
				"79",
				"760",
				"26734",
				"123 Street",
				"123 Street, District 1, HCMC"
		);
		OrderSupportShipment shipment = new OrderSupportShipment(
				UUID.randomUUID(),
				UUID.randomUUID(),
				"SHIPPED",
				"GHN",
				"TRACK123",
				BigDecimal.TEN,
				"STANDARD",
				null,
				Instant.parse("2026-05-20T00:00:00Z"),
				null,
				address,
				List.of()
		);
		OrderSupportDetail detail = new OrderSupportDetail(
				orderId,
				buyerId,
				"SHIPPED",
				"PAID",
				"VNPAY",
				BigDecimal.valueOf(100),
				BigDecimal.valueOf(100),
				Instant.parse("2026-05-19T00:00:00Z"),
				Instant.parse("2026-05-20T00:00:00Z"),
				null,
				null,
				List.of(),
				List.of(shipment),
				List.of()
		);

		OrderSupportDetail masked = OrderSupportDetailPolicy.maskContactFields(detail, false);
		OrderSupportShippingAddress maskedAddress = masked.shipments().getFirst().shippingAddress();

		assertEquals("Nguyen ***", maskedAddress.receiverName());
		assertEquals("***4567", maskedAddress.phone());
		assertEquals("***", maskedAddress.addressDetail());
		assertEquals("***", maskedAddress.fullAddress());
	}

	@Test
	void maskContactFields_keepsOriginalWhenPiiRevealed() {
		OrderSupportShippingAddress address = new OrderSupportShippingAddress(
				"Nguyen Van A",
				"0901234567",
				"79",
				"760",
				"26734",
				"123 Street",
				"123 Street, District 1, HCMC"
		);
		OrderSupportDetail detail = new OrderSupportDetail(
				UUID.randomUUID(),
				UUID.randomUUID(),
				"SHIPPED",
				"PAID",
				"VNPAY",
				BigDecimal.ONE,
				BigDecimal.ONE,
				Instant.now(),
				Instant.now(),
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

		OrderSupportDetail revealed = OrderSupportDetailPolicy.maskContactFields(detail, true);
		assertEquals("Nguyen Van A", revealed.shipments().getFirst().shippingAddress().receiverName());
		assertTrue(revealed.shipments().getFirst().shippingAddress().phone().endsWith("4567"));
	}
}
