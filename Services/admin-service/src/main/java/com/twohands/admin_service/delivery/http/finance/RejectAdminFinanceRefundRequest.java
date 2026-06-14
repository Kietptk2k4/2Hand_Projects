package com.twohands.admin_service.delivery.http.finance;

import com.fasterxml.jackson.annotation.JsonProperty;

public record RejectAdminFinanceRefundRequest(@JsonProperty("admin_note") String adminNote) {
}
