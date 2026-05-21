package com.twohands.commerce_service.domain.order;

import java.util.UUID;

public record ExpiredUnpaidOrderCandidate(UUID orderId, UUID paymentId) {
}
