package com.twohands.commerce_service.application.product.removeproductbyadmin;

import java.util.UUID;

public record RemoveProductByAdminCommand(UUID adminId, UUID productId, String reason) {
}
