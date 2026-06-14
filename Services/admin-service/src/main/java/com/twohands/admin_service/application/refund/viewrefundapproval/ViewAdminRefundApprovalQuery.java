package com.twohands.admin_service.application.refund.viewrefundapproval;

import java.util.UUID;

public record ViewAdminRefundApprovalQuery(UUID refundRequestId, String bearerToken) {
}
