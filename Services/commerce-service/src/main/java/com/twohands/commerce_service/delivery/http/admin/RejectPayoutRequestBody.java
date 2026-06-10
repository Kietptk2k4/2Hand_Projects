package com.twohands.commerce_service.delivery.http.admin;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Size;

public record RejectPayoutRequestBody(
        @JsonProperty("admin_note")
        @Size(max = 1000)
        String adminNote
) {
}
