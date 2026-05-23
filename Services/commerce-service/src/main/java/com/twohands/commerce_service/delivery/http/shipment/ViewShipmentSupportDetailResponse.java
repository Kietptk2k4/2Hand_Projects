package com.twohands.commerce_service.delivery.http.shipment;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.twohands.commerce_service.delivery.http.seller.ShipmentOrderItemSummaryResponse;
import com.twohands.commerce_service.delivery.http.seller.ShippingAddressSnapshotResponse;
import com.twohands.commerce_service.domain.order.OrderStatus;
import com.twohands.commerce_service.domain.shipment.GhnWebhookSummary;
import com.twohands.commerce_service.domain.shipment.ShipmentCarrier;
import com.twohands.commerce_service.domain.shipment.ShipmentStatus;
import com.twohands.commerce_service.domain.shipment.ShipmentStatusHistoryEntry;
import com.twohands.commerce_service.domain.shipment.SellerShipmentRecord;
import com.twohands.commerce_service.domain.shipment.ShipmentAddressSnapshot;
import com.twohands.commerce_service.domain.shipment.ShipmentOrderItemSummary;
import com.twohands.commerce_service.domain.shipment.ViewShipmentSupportDetailResult;
import com.twohands.commerce_service.domain.shipping.ShipmentType;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record ViewShipmentSupportDetailResponse(
        @JsonProperty("shipment_id") UUID shipmentId,
        @JsonProperty("order_id") UUID orderId,
        @JsonProperty("seller_id") UUID sellerId,
        @JsonProperty("buyer_id") UUID buyerId,
        @JsonProperty("order_status") OrderStatus orderStatus,
        ShipmentCarrier carrier,
        @JsonProperty("shipment_type") ShipmentType shipmentType,
        @JsonProperty("internal_status") ShipmentStatus internalStatus,
        @JsonProperty("carrier_status") String carrierStatus,
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
        @JsonProperty("status_history") List<StatusHistoryEntryResponse> statusHistory,
        @JsonProperty("carrier_webhook_events") List<CarrierWebhookEventResponse> carrierWebhookEvents
) {
    public static ViewShipmentSupportDetailResponse from(ViewShipmentSupportDetailResult result) {
        SellerShipmentRecord shipment = result.shipment();
        return new ViewShipmentSupportDetailResponse(
                shipment.shipmentId(),
                shipment.orderId(),
                shipment.sellerId(),
                result.buyerId(),
                result.orderStatus(),
                shipment.carrier(),
                shipment.shipmentType(),
                shipment.status(),
                result.carrierStatus(),
                shipment.ghnOrderCode(),
                shipment.trackingNumber(),
                shipment.shippingFee(),
                shipment.codAmount(),
                shipment.weightGram(),
                shipment.estimatedDeliveryDate(),
                shipment.shippedAt(),
                shipment.deliveredAt(),
                shipment.createdAt(),
                shipment.updatedAt(),
                toAddressResponse(result.shippingAddress()),
                result.orderItems().stream().map(ViewShipmentSupportDetailResponse::toOrderItemResponse).toList(),
                result.statusHistory().stream().map(StatusHistoryEntryResponse::from).toList(),
                result.carrierWebhookEvents().stream().map(CarrierWebhookEventResponse::from).toList()
        );
    }

    private static ShipmentOrderItemSummaryResponse toOrderItemResponse(ShipmentOrderItemSummary item) {
        return new ShipmentOrderItemSummaryResponse(
                item.orderItemId(),
                item.productNameSnapshot(),
                item.quantity(),
                item.status()
        );
    }

    private static ShippingAddressSnapshotResponse toAddressResponse(ShipmentAddressSnapshot address) {
        if (address == null) {
            return null;
        }
        return new ShippingAddressSnapshotResponse(
                address.receiverName(),
                address.phone(),
                address.provinceCode(),
                address.districtCode(),
                address.wardCode(),
                address.addressDetail(),
                address.fullAddress()
        );
    }

    public record StatusHistoryEntryResponse(
            @JsonProperty("old_status") ShipmentStatus oldStatus,
            @JsonProperty("new_status") ShipmentStatus newStatus,
            @JsonProperty("raw_status") String rawStatus,
            @JsonProperty("occurred_at") Instant occurredAt
    ) {
        static StatusHistoryEntryResponse from(ShipmentStatusHistoryEntry entry) {
            return new StatusHistoryEntryResponse(
                    entry.oldStatus(),
                    entry.newStatus(),
                    entry.rawStatus(),
                    entry.occurredAt()
            );
        }
    }

    public record CarrierWebhookEventResponse(
            @JsonProperty("carrier_status") String carrierStatus,
            boolean processed,
            @JsonProperty("received_at") Instant receivedAt
    ) {
        static CarrierWebhookEventResponse from(GhnWebhookSummary summary) {
            return new CarrierWebhookEventResponse(
                    summary.carrierStatus(),
                    summary.processed(),
                    summary.receivedAt()
            );
        }
    }
}
