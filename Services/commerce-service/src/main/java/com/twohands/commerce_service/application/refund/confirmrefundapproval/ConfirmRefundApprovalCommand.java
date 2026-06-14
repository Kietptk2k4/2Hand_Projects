package com.twohands.commerce_service.application.refund.confirmrefundapproval;

import java.util.UUID;

public record ConfirmRefundApprovalCommand(UUID refundRequestId, String adminNote) {
}
