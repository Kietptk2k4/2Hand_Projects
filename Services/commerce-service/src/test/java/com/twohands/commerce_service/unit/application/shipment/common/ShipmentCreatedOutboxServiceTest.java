package com.twohands.commerce_service.unit.application.shipment.common;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.twohands.commerce_service.application.shipment.common.ShipmentCreatedOutboxService;
import com.twohands.commerce_service.domain.outbox.OutboxEvent;
import com.twohands.commerce_service.domain.shipment.ShipmentCarrier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ShipmentCreatedOutboxServiceTest {

    private ShipmentCreatedOutboxService outboxService;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        outboxService = new ShipmentCreatedOutboxService(objectMapper);
    }

    @Test
    void buildShouldIncludeBuyerIdAndTrackingCodeWhenPresent() throws Exception {
        UUID shipmentId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        UUID buyerId = UUID.randomUUID();
        UUID sellerId = UUID.randomUUID();
        UUID orderItemId = UUID.randomUUID();
        Instant createdAt = Instant.parse("2026-06-04T13:00:00Z");

        OutboxEvent event = outboxService.build(
                shipmentId,
                orderId,
                buyerId,
                sellerId,
                ShipmentCarrier.GHN,
                List.of(orderItemId),
                "GHN-TRACK-1",
                createdAt
        );

        JsonNode payload = objectMapper.readTree(event.payload());
        assertThat(payload.get("buyer_id").asText()).isEqualTo(buyerId.toString());
        assertThat(payload.get("shipment_id").asText()).isEqualTo(shipmentId.toString());
        assertThat(payload.get("tracking_code").asText()).isEqualTo("GHN-TRACK-1");
    }

    @Test
    void buildShouldOmitTrackingCodeWhenBlank() throws Exception {
        UUID shipmentId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        UUID buyerId = UUID.randomUUID();
        UUID sellerId = UUID.randomUUID();

        OutboxEvent event = outboxService.build(
                shipmentId,
                orderId,
                buyerId,
                sellerId,
                ShipmentCarrier.MANUAL,
                List.of(UUID.randomUUID()),
                "  ",
                Instant.now()
        );

        JsonNode payload = objectMapper.readTree(event.payload());
        assertThat(payload.has("tracking_code")).isFalse();
    }
}
