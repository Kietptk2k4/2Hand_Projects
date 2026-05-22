package com.twohands.commerce_service.delivery.http.shipment;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.twohands.commerce_service.delivery.http.seller.ShipmentOrderItemSummaryResponse;
import com.twohands.commerce_service.delivery.http.seller.ShippingAddressSnapshotResponse;
import com.twohands.commerce_service.domain.shipment.ShipmentAccessRole;
import com.twohands.commerce_service.domain.shipment.ShipmentCarrier;
import com.twohands.commerce_service.domain.shipment.ShipmentStatus;
import com.twohands.commerce_service.domain.shipment.ShipmentStatusHistoryEntry;
import com.twohands.commerce_service.domain.shipping.ShipmentType;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record ViewShipmentResponse(
        @JsonProperty("shipment_id") UUID shipmentId,
        @JsonProperty("order_id") UUID orderId,
        @JsonProperty("seller_id") UUID sellerId,
        @JsonProperty("accessed_as") ShipmentAccessRole accessedAs,
        ShipmentCarrier carrier,
        @JsonProperty("shipment_type") ShipmentType shipmentType,
        ShipmentStatus status,
        @JsonProperty("ghn_order_code") String ghnOrderCode,
        @JsonProperty("tracking_number") String trackingNumber,
        @JsonProperty("shipping_fee") BigDecimal shippingFee,
        @JsonProperty("cod_amount") BigDecimal codAmount,
        @JsonProperty("weight_gram") Integer weightGram,
        @JsonProperty("estimated_delivery_date") LocalDate estimatedDeliveryDate,
        @JsonProperty("shipped_at") Instant shippedAt,
        @JsonProperty("delivered_at") Instant deliveredAt,
        @JsonProperty("created_at") Instant createdAt,
        @JsonProperty("updated_at") Instant updatedAt,
        @JsonProperty("shipping_address") ShippingAddressSnapshotResponse shippingAddress,
        @JsonProperty("order_items") List<ShipmentOrderItemSummaryResponse> orderItems,
        @JsonProperty("status_history") List<ShipmentStatusHistoryEntryResponse> statusHistory
) {
    public record ShipmentStatusHistoryEntryResponse(
            @JsonProperty("old_status") ShipmentStatus oldStatus,
            @JsonProperty("new_status") ShipmentStatus newStatus,
            @JsonProperty("raw_status") String rawStatus,
            @JsonProperty("occurred_at") Instant occurredAt
    ) {
        static ShipmentStatusHistoryEntryResponse from(ShipmentStatusHistoryEntry entry) {
            return new ShipmentStatusHistoryEntryResponse(
                    entry.oldStatus(),
                    entry.newStatus(),
                    entry.rawStatus(),
                    entry.occurredAt()
            );
        }
    }
}
