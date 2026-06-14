package com.twohands.admin_service.application.refund.rejectrefundapproval;

import java.util.UUID;

public record RejectAdminRefundApprovalCommand(UUID refundRequestId, String adminNote, String bearerToken) {
}
