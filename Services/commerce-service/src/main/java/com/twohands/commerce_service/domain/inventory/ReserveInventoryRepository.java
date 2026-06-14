package com.twohands.commerce_service.domain.inventory;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface ReserveInventoryRepository {

    void reserveAll(List<InventoryReservationLine> lines, Instant updatedAt);

    void syncOutOfStockProductStatuses(List<UUID> productIds, Instant updatedAt);

    void syncInStockProductStatuses(List<UUID> productIds, Instant updatedAt);
}
