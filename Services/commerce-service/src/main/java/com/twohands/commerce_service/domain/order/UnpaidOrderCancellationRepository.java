package com.twohands.commerce_service.domain.order;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface UnpaidOrderCancellationRepository {

    List<ExpiredUnpaidOrderCandidate> findExpiredCandidates(int batchSize, Instant now, Instant orderCreatedBefore);

    boolean hasShipmentBlockingCancel(UUID orderId);

    UnpaidOrderCancelOutcome cancelExpiredUnpaidOrder(UUID orderId, UUID paymentId, Instant now);
}
