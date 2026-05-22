package com.twohands.commerce_service.domain.inventory;

import java.util.UUID;

public record InventoryReservationLine(UUID productId, int quantity) {
}
