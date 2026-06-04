package com.twohands.commerce_service.unit.application.shipment.common;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.twohands.commerce_service.application.shipment.common.ShipmentShippedOutboxService;
import com.twohands.commerce_service.domain.outbox.OutboxEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ShipmentShippedOutboxServiceTest {

    private ShipmentShippedOutboxService outboxService;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        outboxService = new ShipmentShippedOutboxService(objectMapper);
    }

    @Test
    void buildShouldIncludeBuyerIdAndTrackingCode() throws Exception {
        UUID shipmentId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        UUID buyerId = UUID.randomUUID();
        UUID sellerId = UUID.randomUUID();
        Instant shippedAt = Instant.parse("2026-06-04T16:00:00Z");

        OutboxEvent event = outboxService.build(
                shipmentId,
                orderId,
                buyerId,
                sellerId,
                "VN123",
                shippedAt
        );

        assertThat(event.eventType()).isEqualTo(ShipmentShippedOutboxService.EVENT_TYPE);
        assertThat(event.eventKey()).isEqualTo("shipment:" + shipmentId + ":shipped");

        JsonNode payload = objectMapper.readTree(event.payload());
        assertThat(payload.get("buyer_id").asText()).isEqualTo(buyerId.toString());
        assertThat(payload.get("shipment_id").asText()).isEqualTo(shipmentId.toString());
        assertThat(payload.get("order_id").asText()).isEqualTo(orderId.toString());
        assertThat(payload.get("tracking_code").asText()).isEqualTo("VN123");
        assertThat(payload.get("shipped_at").asText()).isEqualTo(shippedAt.toString());
    }

    @Test
    void buildShouldOmitTrackingCodeWhenBlank() throws Exception {
        OutboxEvent event = outboxService.build(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                "  ",
                Instant.now()
        );

        JsonNode payload = objectMapper.readTree(event.payload());
        assertThat(payload.has("tracking_code")).isFalse();
    }
}
