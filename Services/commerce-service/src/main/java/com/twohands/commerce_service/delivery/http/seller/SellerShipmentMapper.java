package com.twohands.commerce_service.delivery.http.seller;

import com.twohands.commerce_service.common.pagination.PageMeta;
import com.twohands.commerce_service.delivery.http.catalog.PageMetaResponse;
import com.twohands.commerce_service.domain.shipment.SellerShipmentDetail;
import com.twohands.commerce_service.domain.shipment.SellerShipmentListEntry;
import com.twohands.commerce_service.domain.shipment.SellerShipmentRecord;
import com.twohands.commerce_service.domain.shipment.ViewSellerShipmentsResult;

final class SellerShipmentMapper {

    private SellerShipmentMapper() {
    }

    static ViewSellerShipmentsResponse toListResponse(ViewSellerShipmentsResult result) {
        PageMeta pagination = result.pagination();
        return new ViewSellerShipmentsResponse(
                result.items().stream().map(SellerShipmentMapper::toListEntryResponse).toList(),
                new PageMetaResponse(
                        pagination.page(),
                        pagination.limit(),
                        pagination.totalItems(),
                        pagination.totalPages(),
                        pagination.hasNext()
                ),
                new SellerShipmentListSummaryResponse(result.statusCounts())
        );
    }

    private static SellerShipmentListEntryResponse toListEntryResponse(SellerShipmentListEntry entry) {
        return new SellerShipmentListEntryResponse(
                entry.shipmentId(),
                entry.orderId(),
                entry.carrier(),
                entry.shipmentType(),
                entry.status(),
                entry.trackingNumber(),
                entry.ghnOrderCode(),
                entry.deliveryAddressSummary(),
                entry.createdAt(),
                entry.updatedAt(),
                entry.orderItemCount()
        );
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
