package com.twohands.auth_service.delivery.http.auth.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

public record ExchangeOAuthCodeRequest(
        @NotBlank
        @JsonProperty("code")
        String code
) {
}
