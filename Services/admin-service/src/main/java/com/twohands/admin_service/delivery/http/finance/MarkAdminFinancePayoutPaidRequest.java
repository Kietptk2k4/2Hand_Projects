package com.twohands.admin_service.delivery.http.finance;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MarkAdminFinancePayoutPaidRequest(
		@JsonProperty("bank_transfer_ref")
		@NotBlank(message = "bank_transfer_ref is required")
		@Size(max = 255)
		String bankTransferRef
) {
}
