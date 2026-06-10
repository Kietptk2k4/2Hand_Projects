package com.twohands.admin_service.delivery.http.finance;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Size;

public record RejectAdminFinancePayoutRequest(
		@JsonProperty("admin_note")
		@Size(max = 1000)
		String adminNote
) {
}
