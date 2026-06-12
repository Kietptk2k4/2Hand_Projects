package com.twohands.commerce_service.application.product.restoreproductbyadmin;

import java.util.UUID;

public record RestoreProductByAdminCommand(UUID adminId, UUID productId, String reason) {
}
