package com.twohands.commerce_service.application.order.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.twohands.commerce_service.domain.outbox.OutboxEvent;
import com.twohands.commerce_service.domain.outbox.OutboxStatus;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Component
public class OrderCompletedOutboxService {

    public static final String EVENT_TYPE = "COMMERCE_ORDER_COMPLETED";
    private static final String SOURCE = "commerce";

    private final ObjectMapper objectMapper;

    public OrderCompletedOutboxService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public OutboxEvent build(
            UUID orderId,
            UUID buyerId,
            List<UUID> sellerIds,
            String reason,
            String completedBy,
            Instant completedAt
    ) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("order_id", orderId.toString());
        payload.put("buyer_id", buyerId.toString());
        payload.put("reason", reason);
        payload.put("completed_at", completedAt.toString());
        payload.put("completed_by", completedBy);
        payload.put("order_code", orderId.toString());

        List<String> distinctSellerIds = distinctSellerIdStrings(sellerIds);
        if (!distinctSellerIds.isEmpty()) {
            payload.put("seller_ids", distinctSellerIds);
        }

        return new OutboxEvent(
                UUID.randomUUID(),
                EVENT_TYPE,
                "order:" + orderId + ":completed",
                orderId,
                SOURCE,
                serialize(payload),
                OutboxStatus.PENDING,
                0,
                completedAt,
                null,
                null
        );
    }

    private List<String> distinctSellerIdStrings(List<UUID> sellerIds) {
        if (sellerIds == null || sellerIds.isEmpty()) {
            return List.of();
        }
        Set<String> distinct = new LinkedHashSet<>();
        for (UUID sellerId : sellerIds) {
            if (sellerId != null) {
                distinct.add(sellerId.toString());
            }
        }
        return new ArrayList<>(distinct);
    }

    private String serialize(Map<String, Object> payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException ex) {
            throw new AppException(ErrorCode.INTERNAL_ERROR, "Cannot serialize outbox payload", ex);
        }
    }
}
