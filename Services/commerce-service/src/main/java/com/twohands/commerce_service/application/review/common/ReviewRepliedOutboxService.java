package com.twohands.commerce_service.application.review.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.twohands.commerce_service.domain.outbox.OutboxEvent;
import com.twohands.commerce_service.domain.outbox.OutboxStatus;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Component
public class ReviewRepliedOutboxService {

    public static final String EVENT_TYPE = "COMMERCE_REVIEW_REPLIED";
    private static final String SOURCE = "commerce";

    private final ObjectMapper objectMapper;

    public ReviewRepliedOutboxService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public OutboxEvent build(
            UUID replyId,
            UUID reviewId,
            UUID sellerId,
            UUID buyerId,
            Instant repliedAt
    ) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("reply_id", replyId.toString());
        payload.put("review_id", reviewId.toString());
        payload.put("seller_id", sellerId.toString());
        payload.put("buyer_id", buyerId.toString());
        payload.put("replied_at", repliedAt.toString());

        return new OutboxEvent(
                UUID.randomUUID(),
                EVENT_TYPE,
                "review:" + reviewId + ":replied",
                reviewId,
                SOURCE,
                serialize(payload),
                OutboxStatus.PENDING,
                0,
                repliedAt,
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
