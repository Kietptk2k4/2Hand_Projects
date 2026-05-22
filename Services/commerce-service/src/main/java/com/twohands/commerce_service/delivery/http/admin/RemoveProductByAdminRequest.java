package com.twohands.commerce_service.delivery.http.admin;

import jakarta.validation.constraints.NotBlank;

public record RemoveProductByAdminRequest(@NotBlank String reason) {
}
