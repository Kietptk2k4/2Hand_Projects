package com.twohands.admin_service.application.refund.confirmrefundapproval;

import java.util.UUID;

public record ConfirmAdminRefundApprovalCommand(UUID refundRequestId, String adminNote, String bearerToken) {
}
