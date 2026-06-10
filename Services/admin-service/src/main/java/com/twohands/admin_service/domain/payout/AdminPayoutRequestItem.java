package com.twohands.admin_service.domain.payout;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record AdminPayoutRequestItem(
        UUID id,
        UUID sellerId,
        UUID payoutAccountId,
        BigDecimal amount,
        String status,
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
