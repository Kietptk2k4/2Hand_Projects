package com.twohands.admin_service.domain.payout;

import java.util.List;

public record AdminPayoutRequestListResult(
        List<AdminPayoutRequestItem> items,
        int page,
        int limit,
        long totalItems,
        int totalPages,
        boolean hasNext
) {
}
