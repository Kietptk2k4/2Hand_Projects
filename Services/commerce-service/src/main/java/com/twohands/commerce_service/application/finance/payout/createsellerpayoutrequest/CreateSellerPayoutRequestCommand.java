package com.twohands.commerce_service.application.finance.payout.createsellerpayoutrequest;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateSellerPayoutRequestCommand(UUID sellerId, UUID payoutAccountId, BigDecimal amount) {
}
