package com.twohands.commerce_service.unit.application.finance.payout.common;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.twohands.commerce_service.application.finance.payout.common.PayoutRequestRejectedOutboxService;
import com.twohands.commerce_service.domain.outbox.OutboxEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PayoutRequestRejectedOutboxServiceTest {

    private PayoutRequestRejectedOutboxService outboxService;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        outboxService = new PayoutRequestRejectedOutboxService(objectMapper);
    }

    @Test
    void buildShouldIncludeSellerAmountAndAdminNote() throws Exception {
        UUID payoutRequestId = UUID.randomUUID();
        UUID sellerId = UUID.randomUUID();
        Instant rejectedAt = Instant.parse("2026-06-04T14:00:00Z");

        OutboxEvent event = outboxService.build(
                payoutRequestId,
                sellerId,
                new BigDecimal("150000"),
                "Thông tin tài khoản không hợp lệ",
                rejectedAt
        );

        assertThat(event.eventType()).isEqualTo("COMMERCE_PAYOUT_REQUEST_REJECTED");

        JsonNode payload = objectMapper.readTree(event.payload());
        assertThat(payload.get("payout_request_id").asText()).isEqualTo(payoutRequestId.toString());
        assertThat(payload.get("seller_id").asText()).isEqualTo(sellerId.toString());
        assertThat(payload.get("amount").decimalValue()).isEqualByComparingTo("150000");
        assertThat(payload.get("admin_note").asText()).isEqualTo("Thông tin tài khoản không hợp lệ");
    }
}
