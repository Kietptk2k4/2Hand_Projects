package com.twohands.commerce_service.unit.application.payment.common;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.twohands.commerce_service.application.payment.common.PaymentFailedOutboxService;
import com.twohands.commerce_service.domain.outbox.OutboxEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PaymentFailedOutboxServiceTest {

    private PaymentFailedOutboxService outboxService;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        outboxService = new PaymentFailedOutboxService(objectMapper);
    }

    @Test
    void buildShouldIncludeBuyerId() throws Exception {
        UUID paymentId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        UUID buyerId = UUID.randomUUID();
        Instant failedAt = Instant.parse("2026-06-04T12:00:00Z");

        OutboxEvent event = outboxService.build(
                paymentId,
                orderId,
                buyerId,
                "PAYOS_WEBHOOK_FAILED",
                failedAt
        );

        JsonNode payload = objectMapper.readTree(event.payload());
        assertThat(payload.get("buyer_id").asText()).isEqualTo(buyerId.toString());
        assertThat(payload.get("payment_id").asText()).isEqualTo(paymentId.toString());
        assertThat(payload.get("order_id").asText()).isEqualTo(orderId.toString());
        assertThat(payload.get("order_code").asText()).isEqualTo(orderId.toString());
    }
}
