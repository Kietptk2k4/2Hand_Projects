package com.twohands.commerce_service.application.finance.payout.rejectpayoutrequest;

import java.util.UUID;

public record RejectPayoutRequestCommand(UUID payoutRequestId, String adminNote) {
}
