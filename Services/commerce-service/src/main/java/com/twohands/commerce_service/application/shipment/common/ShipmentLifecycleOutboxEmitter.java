package com.twohands.commerce_service.application.shipment.common;

import com.twohands.commerce_service.domain.order.OrderBuyerRepository;
import com.twohands.commerce_service.domain.outbox.OutboxEventRepository;
import com.twohands.commerce_service.domain.shipment.SellerShipmentRecord;
import com.twohands.commerce_service.domain.shipment.ShipmentStatus;
import com.twohands.commerce_service.domain.shipment.ShipmentTrackingCodeSanitizer;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
public class ShipmentLifecycleOutboxEmitter {

    private final OutboxEventRepository outboxEventRepository;
    private final ShipmentShippedOutboxService shipmentShippedOutboxService;
    private final ShipmentDeliveredOutboxService shipmentDeliveredOutboxService;
    private final OrderBuyerRepository orderBuyerRepository;

    public ShipmentLifecycleOutboxEmitter(
            OutboxEventRepository outboxEventRepository,
            ShipmentShippedOutboxService shipmentShippedOutboxService,
            ShipmentDeliveredOutboxService shipmentDeliveredOutboxService,
            OrderBuyerRepository orderBuyerRepository
    ) {
        this.outboxEventRepository = outboxEventRepository;
        this.shipmentShippedOutboxService = shipmentShippedOutboxService;
        this.shipmentDeliveredOutboxService = shipmentDeliveredOutboxService;
        this.orderBuyerRepository = orderBuyerRepository;
    }

    public void emitDedicatedNotificationEvents(
            SellerShipmentRecord shipment,
            ShipmentStatus newStatus,
            Instant occurredAt,
            String trackingOverride
    ) {
        Map<UUID, UUID> buyerIdCache = new HashMap<>();
        if (newStatus != ShipmentStatus.SHIPPED && newStatus != ShipmentStatus.DELIVERED) {
            return;
        }

        UUID buyerId = resolveBuyerId(shipment.orderId(), buyerIdCache);
        String trackingCode = resolveTrackingCode(shipment, trackingOverride);

        if (newStatus == ShipmentStatus.SHIPPED) {
            outboxEventRepository.save(shipmentShippedOutboxService.build(
                    shipment.shipmentId(),
                    shipment.orderId(),
                    buyerId,
                    shipment.sellerId(),
                    trackingCode,
                    occurredAt
            ));
        }

        if (newStatus == ShipmentStatus.DELIVERED) {
            outboxEventRepository.save(shipmentDeliveredOutboxService.build(
                    shipment.shipmentId(),
                    shipment.orderId(),
                    buyerId,
                    shipment.sellerId(),
                    trackingCode,
                    occurredAt
            ));
        }
    }

    private UUID resolveBuyerId(UUID orderId, Map<UUID, UUID> buyerIdCache) {
        return buyerIdCache.computeIfAbsent(orderId, id -> orderBuyerRepository.findBuyerIdByOrderId(id)
                .orElseThrow(() -> new AppException(
                        ErrorCode.INTERNAL_ERROR,
                        "Order buyer not found for order " + id
                )));
    }

    private String resolveTrackingCode(SellerShipmentRecord shipment, String trackingOverride) {
        if (StringUtils.hasText(trackingOverride)) {
            return ShipmentTrackingCodeSanitizer.sanitize(trackingOverride);
        }
        String fromRecord = firstNonBlank(shipment.trackingNumber(), shipment.ghnOrderCode());
        return ShipmentTrackingCodeSanitizer.sanitize(fromRecord);
    }

    private String firstNonBlank(String first, String second) {
        if (StringUtils.hasText(first)) {
            return first;
        }
        if (StringUtils.hasText(second)) {
            return second;
        }
        return null;
    }
}
