package com.twohands.admin_service.unit.support;

import com.twohands.admin_service.domain.support.ShipmentSupportDetail;
import com.twohands.admin_service.domain.support.ShipmentSupportDetailPolicy;
import com.twohands.admin_service.domain.support.ShipmentSupportShippingAddress;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ShipmentSupportDetailPolicyTest {

	@Test
	void maskContactFields_masksShippingAddress() {
		ShipmentSupportDetail detail = new ShipmentSupportDetail(
				UUID.randomUUID(),
				UUID.randomUUID(),
				UUID.randomUUID(),
				UUID.randomUUID(),
				"PROCESSING",
				"GHN",
				"STANDARD",
				"SHIPPED",
				"transporting",
				"GHN-1",
				"TRACK-1",
				BigDecimal.TEN,
				BigDecimal.ZERO,
				500,
				null,
				Instant.parse("2026-05-20T08:00:00Z"),
				null,
				Instant.now(),
				Instant.now(),
				new ShipmentSupportShippingAddress(
						"Tran Van B",
						"0909876543",
						"79",
						"760",
						"1",
						"123 Street",
						"Full address"
				),
				List.of(),
				List.of(),
				List.of()
		);

		ShipmentSupportDetail masked = ShipmentSupportDetailPolicy.maskContactFields(detail, false);

		assertEquals("Tran ***", masked.shippingAddress().receiverName());
		assertEquals("***6543", masked.shippingAddress().phone());
		assertEquals("***", masked.shippingAddress().addressDetail());
	}
}
