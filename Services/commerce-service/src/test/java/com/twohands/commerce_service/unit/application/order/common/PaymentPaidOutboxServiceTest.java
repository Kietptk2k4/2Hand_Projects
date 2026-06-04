package com.twohands.commerce_service.unit.application.order.common;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.twohands.commerce_service.application.order.common.PaymentPaidOutboxService;
import com.twohands.commerce_service.domain.outbox.OutboxEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PaymentPaidOutboxServiceTest {

    private PaymentPaidOutboxService outboxService;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        outboxService = new PaymentPaidOutboxService(objectMapper);
    }

    @Test
    void buildShouldIncludeBuyerIdAndOrderCode() throws Exception {
        UUID paymentId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        UUID buyerId = UUID.randomUUID();
        Instant paidAt = Instant.parse("2026-06-04T11:00:00Z");

        OutboxEvent event = outboxService.build(
                paymentId,
                orderId,
                buyerId,
                "PAYOS_WEBHOOK_PAID",
                paidAt,
                orderId.toString()
        );

        JsonNode payload = objectMapper.readTree(event.payload());
        assertThat(payload.get("payment_id").asText()).isEqualTo(paymentId.toString());
        assertThat(payload.get("order_id").asText()).isEqualTo(orderId.toString());
        assertThat(payload.get("buyer_id").asText()).isEqualTo(buyerId.toString());
        assertThat(payload.get("order_code").asText()).isEqualTo(orderId.toString());
        assertThat(payload.get("reason").asText()).isEqualTo("PAYOS_WEBHOOK_PAID");
        assertThat(payload.get("paid_at").asText()).isEqualTo(paidAt.toString());
    }
}
