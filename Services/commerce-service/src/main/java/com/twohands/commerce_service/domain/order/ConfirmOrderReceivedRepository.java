package com.twohands.commerce_service.domain.order;

import java.time.Instant;
import java.util.UUID;

public interface ConfirmOrderReceivedRepository {

    ConfirmOrderReceivedResult confirmReceivedByBuyer(UUID buyerId, UUID orderId, Instant now);
}
