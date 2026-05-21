package com.twohands.commerce_service.domain.order;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface DeliveredOrderCompletionRepository {

    List<StaleDeliveredOrderItemCandidate> findStaleDeliveredItems(int batchSize, Instant deliveredBefore);

    DeliveredOrderCompletionResult completeDeliveredItemsForOrder(
            UUID orderId,
            List<UUID> orderItemIds,
            Instant now
    );
}
