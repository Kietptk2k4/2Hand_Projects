package com.twohands.commerce_service.application.finance.payout.markpayoutrequestpaid;

import java.util.UUID;

public record MarkPayoutRequestPaidCommand(UUID payoutRequestId, String bankTransferRef) {
}
