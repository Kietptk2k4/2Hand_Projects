package com.twohands.admin_service.application.finance.rejectpayoutrequest;

import java.util.UUID;

public record RejectAdminFinancePayoutCommand(UUID payoutRequestId, String adminNote, String bearerToken) {
}
