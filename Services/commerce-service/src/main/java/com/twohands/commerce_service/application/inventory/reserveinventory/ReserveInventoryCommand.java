package com.twohands.commerce_service.application.inventory.reserveinventory;

import com.twohands.commerce_service.domain.inventory.InventoryReservationLine;

import java.time.Instant;
import java.util.List;

public record ReserveInventoryCommand(List<InventoryReservationLine> lines, Instant occurredAt) {
}
