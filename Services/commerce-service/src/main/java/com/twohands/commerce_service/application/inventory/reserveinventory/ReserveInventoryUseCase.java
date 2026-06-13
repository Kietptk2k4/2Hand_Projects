package com.twohands.commerce_service.application.inventory.reserveinventory;

import com.twohands.commerce_service.application.cart.synccartitemstatus.SyncCartItemStatusUseCase;
import com.twohands.commerce_service.domain.inventory.InventoryReservationLine;
import com.twohands.commerce_service.domain.inventory.ReserveInventoryRepository;
import com.twohands.commerce_service.domain.order.OrderItemQuantity;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class ReserveInventoryUseCase {

    private final ReserveInventoryRepository reserveInventoryRepository;
    private final SyncCartItemStatusUseCase syncCartItemStatusUseCase;

    public ReserveInventoryUseCase(
            ReserveInventoryRepository reserveInventoryRepository,
            SyncCartItemStatusUseCase syncCartItemStatusUseCase
    ) {
        this.reserveInventoryRepository = reserveInventoryRepository;
        this.syncCartItemStatusUseCase = syncCartItemStatusUseCase;
    }

    @Transactional
    public ReserveInventoryResult execute(ReserveInventoryCommand command) {
        List<InventoryReservationLine> normalized = normalizeLines(command.lines());
        if (normalized.isEmpty()) {
            return new ReserveInventoryResult(List.of(), command.occurredAt());
        }

        validateQuantities(normalized);
        reserveInventoryRepository.reserveAll(normalized, command.occurredAt());

        List<UUID> productIds = normalized.stream()
                .map(InventoryReservationLine::productId)
                .distinct()
                .toList();
        reserveInventoryRepository.syncOutOfStockProductStatuses(productIds, command.occurredAt());
        for (UUID productId : productIds) {
            syncCartItemStatusUseCase.syncByProductId(productId);
        }

        List<OrderItemQuantity> reservedItems = normalized.stream()
                .map(line -> new OrderItemQuantity(line.productId(), line.productId(), line.quantity()))
                .toList();

        return new ReserveInventoryResult(reservedItems, command.occurredAt());
    }

    private List<InventoryReservationLine> normalizeLines(List<InventoryReservationLine> lines) {
        Map<UUID, Integer> quantityByProduct = new LinkedHashMap<>();
        for (InventoryReservationLine line : lines) {
            quantityByProduct.merge(line.productId(), line.quantity(), Integer::sum);
        }
        return quantityByProduct.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> new InventoryReservationLine(entry.getKey(), entry.getValue()))
                .toList();
    }

    private void validateQuantities(List<InventoryReservationLine> lines) {
        for (InventoryReservationLine line : lines) {
            if (line.quantity() <= 0) {
                throw new AppException(
                        ErrorCode.VALIDATION_ERROR,
                        "Reservation quantity must be greater than 0",
                        "quantity",
                        "must be greater than 0"
                );
            }
        }
    }
}
