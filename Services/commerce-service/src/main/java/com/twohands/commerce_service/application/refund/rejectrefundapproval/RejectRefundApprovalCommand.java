package com.twohands.commerce_service.application.refund.rejectrefundapproval;

import java.util.UUID;

public record RejectRefundApprovalCommand(UUID refundRequestId, String adminNote) {
}
