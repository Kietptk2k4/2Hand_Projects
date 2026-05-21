package com.twohands.commerce_service.domain.shipment;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CreateShipmentRepository {

    Optional<CreateShipmentOrderContext> findOrderContext(UUID orderId);

    Optional<BuyerDeliveryAddress> findBuyerDeliveryAddress(UUID buyerId);

    Optional<SellerPickupAddress> findSellerPickupBySellerId(UUID sellerId);

    List<ShipmentOrderItemLine> findOrderItemsForSeller(
            UUID orderId,
            UUID sellerId,
            List<UUID> orderItemIds
    );

    CreateShipmentResult createShipment(CreateShipmentDraft draft, Instant occurredAt);

    void updateGhnProviderFields(
            UUID shipmentId,
            GhnCreateOrderResult ghnResult,
            Instant occurredAt
    );
}
