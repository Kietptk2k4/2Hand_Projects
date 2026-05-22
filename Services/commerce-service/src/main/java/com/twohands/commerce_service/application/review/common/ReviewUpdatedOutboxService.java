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
public class ReviewUpdatedOutboxService {

    public static final String EVENT_TYPE = "COMMERCE_REVIEW_UPDATED";
    private static final String SOURCE = "commerce";

    private final ObjectMapper objectMapper;

    public ReviewUpdatedOutboxService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public OutboxEvent build(
            UUID reviewId,
            UUID orderItemId,
            UUID sellerId,
            UUID buyerId,
            int previousRating,
            int rating,
            boolean ratingChanged,
            Instant updatedAt
    ) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("review_id", reviewId.toString());
        payload.put("order_item_id", orderItemId.toString());
        payload.put("seller_id", sellerId.toString());
        payload.put("buyer_id", buyerId.toString());
        payload.put("previous_rating", previousRating);
        payload.put("rating", rating);
        payload.put("rating_changed", ratingChanged);
        payload.put("updated_at", updatedAt.toString());

        return new OutboxEvent(
                UUID.randomUUID(),
                EVENT_TYPE,
                "review:" + reviewId + ":updated",
                reviewId,
                SOURCE,
                serialize(payload),
                OutboxStatus.PENDING,
                0,
                updatedAt,
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
