package com.twohands.admin_service.application.finance.markpayoutpaid;

import java.util.UUID;

public record MarkAdminFinancePayoutPaidCommand(UUID payoutRequestId, String bankTransferRef, String bearerToken) {
}
