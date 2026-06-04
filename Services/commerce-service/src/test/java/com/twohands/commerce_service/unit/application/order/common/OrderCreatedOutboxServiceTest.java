package com.twohands.commerce_service.unit.application.order.common;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.twohands.commerce_service.application.order.common.OrderCreatedOutboxService;
import com.twohands.commerce_service.domain.outbox.OutboxEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class OrderCreatedOutboxServiceTest {

    private OrderCreatedOutboxService outboxService;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        outboxService = new OrderCreatedOutboxService(objectMapper);
    }

    @Test
    void buildShouldIncludeBuyerIdAndDistinctSellerIds() throws Exception {
        UUID orderId = UUID.randomUUID();
        UUID buyerId = UUID.randomUUID();
        UUID sellerA = UUID.randomUUID();
        UUID sellerB = UUID.randomUUID();
        Instant now = Instant.parse("2026-06-04T10:00:00Z");

        OutboxEvent event = outboxService.build(
                orderId,
                buyerId,
                List.of(sellerA, sellerA, sellerB),
                BigDecimal.valueOf(1_000_000),
                "PAYOS",
                now
        );

        assertThat(event.eventType()).isEqualTo(OrderCreatedOutboxService.EVENT_TYPE);

        JsonNode payload = objectMapper.readTree(event.payload());
        assertThat(payload.get("order_id").asText()).isEqualTo(orderId.toString());
        assertThat(payload.get("buyer_id").asText()).isEqualTo(buyerId.toString());
        assertThat(payload.get("order_code").asText()).isEqualTo(orderId.toString());
        assertThat(payload.get("seller_ids")).hasSize(2);
        assertThat(payload.get("seller_ids").get(0).asText()).isEqualTo(sellerA.toString());
        assertThat(payload.get("seller_ids").get(1).asText()).isEqualTo(sellerB.toString());
    }
}
