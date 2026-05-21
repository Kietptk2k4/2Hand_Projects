package com.twohands.commerce_service.domain.order;

import java.time.Instant;
import java.util.UUID;

public interface OrderCompletionRepository {

    CompleteOrderResult completeIfEligible(
            UUID orderId,
            String reason,
            String changedBy,
            String completedByOutbox,
            Instant now
    );
}
