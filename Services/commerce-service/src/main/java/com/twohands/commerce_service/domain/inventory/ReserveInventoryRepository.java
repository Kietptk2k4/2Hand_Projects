package com.twohands.commerce_service.domain.inventory;

import java.time.Instant;
import java.util.List;

public interface ReserveInventoryRepository {

    void reserveAll(List<InventoryReservationLine> lines, Instant updatedAt);
}
