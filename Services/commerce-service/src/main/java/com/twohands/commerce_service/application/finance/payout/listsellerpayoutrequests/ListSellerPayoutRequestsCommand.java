package com.twohands.commerce_service.application.finance.payout.listsellerpayoutrequests;

import com.twohands.commerce_service.domain.finance.PayoutRequestStatus;

import java.util.Optional;
import java.util.UUID;

public record ListSellerPayoutRequestsCommand(
        UUID sellerId,
        Optional<PayoutRequestStatus> status,
        Integer page,
        Integer limit
) {
}
