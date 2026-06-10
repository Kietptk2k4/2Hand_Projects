package com.twohands.commerce_service.application.finance.payout.cancelsellerpayoutrequest;

import java.util.UUID;

public record CancelSellerPayoutRequestCommand(UUID sellerId, UUID payoutRequestId) {
}
