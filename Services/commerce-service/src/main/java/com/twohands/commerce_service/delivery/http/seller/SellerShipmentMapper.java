package com.twohands.commerce_service.delivery.http.seller;

import com.twohands.commerce_service.domain.shipment.SellerShipmentDetail;
import com.twohands.commerce_service.domain.shipment.SellerShipmentRecord;

final class SellerShipmentMapper {

    private SellerShipmentMapper() {
    }

    static SellerShipmentDetailResponse toDetailResponse(SellerShipmentDetail detail) {
        SellerShipmentRecord shipment = detail.shipment();
        return new SellerShipmentDetailResponse(
                shipment.shipmentId(),
                shipment.orderId(),
                shipment.sellerId(),
                shipment.carrier(),
                shipment.shipmentType(),
                shipment.status(),
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
                new ShippingAddressSnapshotResponse(
                        detail.addressSnapshot().receiverName(),
                        detail.addressSnapshot().phone(),
                        detail.addressSnapshot().provinceCode(),
                        detail.addressSnapshot().districtCode(),
                        detail.addressSnapshot().wardCode(),
                        detail.addressSnapshot().addressDetail(),
                        detail.addressSnapshot().fullAddress()
                ),
                detail.orderItems().stream()
                        .map(item -> new ShipmentOrderItemSummaryResponse(
                                item.orderItemId(),
                                item.productNameSnapshot(),
                                item.quantity(),
                                item.status()
                        ))
                        .toList()
        );
    }
}
