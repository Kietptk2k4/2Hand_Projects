package com.twohands.commerce_service.unit.domain.shipment;

import com.twohands.commerce_service.domain.shipment.GhnWebhookSummary;
import com.twohands.commerce_service.domain.shipment.ShipmentCarrierStatusResolver;
import com.twohands.commerce_service.domain.shipment.ShipmentStatus;
import com.twohands.commerce_service.domain.shipment.ShipmentStatusHistoryEntry;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ShipmentCarrierStatusResolverTest {

	@Test
	void resolve_prefersLatestWebhookStatus() {
		List<GhnWebhookSummary> webhooks = List.of(
				new GhnWebhookSummary("delivered", true, Instant.parse("2026-05-21T10:00:00Z")),
				new GhnWebhookSummary("picking", true, Instant.parse("2026-05-20T10:00:00Z"))
		);
		List<ShipmentStatusHistoryEntry> history = List.of(
				new ShipmentStatusHistoryEntry(
						ShipmentStatus.PENDING,
						ShipmentStatus.SHIPPED,
						"picking",
						Instant.parse("2026-05-19T10:00:00Z")
				)
		);

		assertEquals("delivered", ShipmentCarrierStatusResolver.resolve(webhooks, history));
	}

	@Test
	void resolve_fallsBackToHistoryRawStatus() {
		List<ShipmentStatusHistoryEntry> history = List.of(
				new ShipmentStatusHistoryEntry(
						null,
						ShipmentStatus.PENDING,
						"storing",
						Instant.parse("2026-05-19T10:00:00Z")
				)
		);

		assertEquals("storing", ShipmentCarrierStatusResolver.resolve(List.of(), history));
	}

	@Test
	void resolve_returnsNullWhenNoCarrierSignals() {
		assertNull(ShipmentCarrierStatusResolver.resolve(List.of(), List.of()));
	}
}
