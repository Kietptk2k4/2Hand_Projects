package com.twohands.commerce_service.domain.finance;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record SellerPayoutRequest(
        UUID id,
        UUID sellerId,
        UUID payoutAccountId,
        BigDecimal amount,
        PayoutRequestStatus status,
        String adminNote,
        String bankTransferRef,
        Instant requestedAt,
        Instant approvedAt,
        Instant paidAt,
        Instant rejectedAt,
        Instant cancelledAt,
        String bankName,
        String bankAccountName,
        String bankAccountNumber
) {
}
