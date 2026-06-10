package com.twohands.admin_service.application.finance.approvepayoutrequest;

import java.util.UUID;

public record ApproveAdminFinancePayoutCommand(UUID payoutRequestId, String bearerToken) {
}
