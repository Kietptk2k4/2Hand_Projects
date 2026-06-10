package com.twohands.admin_service.delivery.http.support;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AdminOverrideShipmentStatusRequest(
		@NotBlank
		String status,

		@NotBlank
		@Size(min = 10, max = 500)
		String reason,

		Boolean force
) {
	public boolean forceOrDefault() {
		return force != null && force;
	}
}
