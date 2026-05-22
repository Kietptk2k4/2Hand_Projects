package com.twohands.commerce_service.application.inventory.reserveinventory;

import com.twohands.commerce_service.domain.order.OrderItemQuantity;

import java.time.Instant;
import java.util.List;

public record ReserveInventoryResult(List<OrderItemQuantity> reservedItems, Instant reservedAt) {
}
