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
public class PayoutRequestRejectedOutboxService {

    public static final String EVENT_TYPE = "COMMERCE_PAYOUT_REQUEST_REJECTED";
    private static final String SOURCE = "commerce";

    private final ObjectMapper objectMapper;

    public PayoutRequestRejectedOutboxService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public OutboxEvent build(
            UUID payoutRequestId,
            UUID sellerId,
            BigDecimal amount,
            String adminNote,
            Instant rejectedAt
    ) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("payout_request_id", payoutRequestId.toString());
        payload.put("seller_id", sellerId.toString());
        payload.put("amount", amount);
        payload.put("admin_note", adminNote);
        payload.put("rejected_at", rejectedAt.toString());

        return new OutboxEvent(
                UUID.randomUUID(),
                EVENT_TYPE,
                "payout-request:" + payoutRequestId + ":rejected",
                payoutRequestId,
                SOURCE,
                serialize(payload),
                OutboxStatus.PENDING,
                0,
                rejectedAt,
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
