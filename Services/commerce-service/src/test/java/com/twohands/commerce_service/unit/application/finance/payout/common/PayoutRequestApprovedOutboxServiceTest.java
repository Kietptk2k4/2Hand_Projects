package com.twohands.commerce_service.unit.application.finance.payout.common;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.twohands.commerce_service.application.finance.payout.common.PayoutRequestApprovedOutboxService;
import com.twohands.commerce_service.domain.outbox.OutboxEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PayoutRequestApprovedOutboxServiceTest {

    private PayoutRequestApprovedOutboxService outboxService;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        outboxService = new PayoutRequestApprovedOutboxService(objectMapper);
    }

    @Test
    void buildShouldIncludeSellerAndAmount() throws Exception {
        UUID payoutRequestId = UUID.randomUUID();
        UUID sellerId = UUID.randomUUID();
        Instant approvedAt = Instant.parse("2026-06-04T14:00:00Z");

        OutboxEvent event = outboxService.build(
                payoutRequestId,
                sellerId,
                new BigDecimal("150000"),
                approvedAt
        );

        assertThat(event.eventType()).isEqualTo("COMMERCE_PAYOUT_REQUEST_APPROVED");

        JsonNode payload = objectMapper.readTree(event.payload());
        assertThat(payload.get("payout_request_id").asText()).isEqualTo(payoutRequestId.toString());
        assertThat(payload.get("seller_id").asText()).isEqualTo(sellerId.toString());
        assertThat(payload.get("amount").decimalValue()).isEqualByComparingTo("150000");
    }
}
