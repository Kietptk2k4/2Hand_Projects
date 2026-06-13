package com.twohands.commerce_service.unit.application.order.common;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.twohands.commerce_service.application.order.common.OrderCompletedOutboxService;
import com.twohands.commerce_service.domain.outbox.OutboxEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class OrderCompletedOutboxServiceTest {

    private OrderCompletedOutboxService outboxService;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        outboxService = new OrderCompletedOutboxService(objectMapper);
    }

    @Test
    void buildShouldIncludeBuyerId() throws Exception {
        UUID orderId = UUID.randomUUID();
        UUID buyerId = UUID.randomUUID();
        Instant completedAt = Instant.parse("2026-06-04T14:00:00Z");

        OutboxEvent event = outboxService.build(
                orderId,
                buyerId,
                List.of(),
                "BUYER_CONFIRM_RECEIVED",
                "BUYER",
                completedAt
        );

        JsonNode payload = objectMapper.readTree(event.payload());
        assertThat(payload.get("order_id").asText()).isEqualTo(orderId.toString());
        assertThat(payload.get("buyer_id").asText()).isEqualTo(buyerId.toString());
        assertThat(payload.get("order_code").asText()).isEqualTo(orderId.toString());
    }
}
