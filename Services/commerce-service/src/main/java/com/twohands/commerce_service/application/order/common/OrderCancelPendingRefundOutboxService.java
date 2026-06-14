package com.twohands.commerce_service.application.order.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.twohands.commerce_service.domain.outbox.OutboxEvent;
import com.twohands.commerce_service.domain.outbox.OutboxStatus;
import com.twohands.commerce_service.domain.payment.PaymentRefundRequestedBy;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
public class OrderCancelPendingRefundOutboxService {

    public static final String EVENT_TYPE = "COMMERCE_ORDER_CANCEL_PENDING_REFUND";
    private static final String SOURCE = "commerce";

    private final ObjectMapper objectMapper;

    public OrderCancelPendingRefundOutboxService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public OutboxEvent build(
            UUID refundRequestId,
            UUID orderId,
            UUID paymentId,
            UUID buyerId,
            PaymentRefundRequestedBy requestedBy,
            UUID requestedByUserId,
            BigDecimal amount,
            String reason,
            Instant requestedAt
    ) {
        return build(
                refundRequestId,
                orderId,
                paymentId,
                buyerId,
                List.of(),
                requestedBy,
                requestedByUserId,
                amount,
                reason,
                requestedAt
        );
    }

    public OutboxEvent build(
            UUID refundRequestId,
            UUID orderId,
            UUID paymentId,
            UUID buyerId,
            List<UUID> sellerIds,
            PaymentRefundRequestedBy requestedBy,
            UUID requestedByUserId,
            BigDecimal amount,
            String reason,
            Instant requestedAt
    ) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("refund_request_id", refundRequestId.toString());
        payload.put("order_id", orderId.toString());
        payload.put("payment_id", paymentId.toString());
        payload.put("buyer_id", buyerId.toString());
        List<String> distinctSellerIds = distinctSellerIdStrings(sellerIds);
        if (!distinctSellerIds.isEmpty()) {
            payload.put("seller_ids", distinctSellerIds);
        }
        payload.put("requested_by", requestedBy.name());
        payload.put("requested_by_user_id", requestedByUserId.toString());
        payload.put("amount", amount);
        payload.put("reason", reason);
        payload.put("requested_at", requestedAt.toString());

        return new OutboxEvent(
                UUID.randomUUID(),
                EVENT_TYPE,
                "order:" + orderId + ":cancel_pending_refund",
                orderId,
                SOURCE,
                serialize(payload),
                OutboxStatus.PENDING,
                0,
                requestedAt,
                null,
                null
        );
    }

    private List<String> distinctSellerIdStrings(List<UUID> sellerIds) {
        if (sellerIds == null || sellerIds.isEmpty()) {
            return List.of();
        }
        List<String> values = new ArrayList<>();
        for (UUID sellerId : sellerIds) {
            if (sellerId == null) {
                continue;
            }
            String value = sellerId.toString();
            if (!values.contains(value)) {
                values.add(value);
            }
        }
        return values;
    }

    private String serialize(Map<String, Object> payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException ex) {
            throw new AppException(ErrorCode.INTERNAL_ERROR, "Cannot serialize outbox payload", ex);
        }
    }
}
