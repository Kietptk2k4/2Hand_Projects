package com.twohands.commerce_service.application.finance.payout.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.twohands.commerce_service.domain.outbox.OutboxEvent;
import com.twohands.commerce_service.domain.outbox.OutboxStatus;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Component
public class PayoutRequestApprovedOutboxService {

    public static final String EVENT_TYPE = "COMMERCE_PAYOUT_REQUEST_APPROVED";
    private static final String SOURCE = "commerce";

    private final ObjectMapper objectMapper;

    public PayoutRequestApprovedOutboxService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public OutboxEvent build(
            UUID payoutRequestId,
            UUID sellerId,
            BigDecimal amount,
            Instant approvedAt
    ) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("payout_request_id", payoutRequestId.toString());
        payload.put("seller_id", sellerId.toString());
        payload.put("amount", amount);
        payload.put("approved_at", approvedAt.toString());

        return new OutboxEvent(
                UUID.randomUUID(),
                EVENT_TYPE,
                "payout-request:" + payoutRequestId + ":approved",
                payoutRequestId,
                SOURCE,
                serialize(payload),
                OutboxStatus.PENDING,
                0,
                approvedAt,
                null,
                null
        );
    }

    private String serialize(Map<String, Object> payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException ex) {
            throw new AppException(ErrorCode.INTERNAL_ERROR, "Cannot serialize outbox payload", ex);
        }
    }
}
