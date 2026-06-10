package com.twohands.commerce_service.application.shipment.common;

import com.twohands.commerce_service.domain.outbox.OutboxEventRepository;
import com.twohands.commerce_service.domain.shipment.AdminShipmentStatusOverridePolicy;
import com.twohands.commerce_service.domain.shipment.ProcessGhnWebhookRepository;
import com.twohands.commerce_service.domain.shipment.SellerShipmentRecord;
import com.twohands.commerce_service.domain.shipment.ShipmentStatus;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;

@Service
public class ShipmentStatusTransitionService {

    private final ProcessGhnWebhookRepository processGhnWebhookRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final ShipmentStatusChangedOutboxService shipmentStatusChangedOutboxService;
    private final ShipmentLifecycleOutboxEmitter shipmentLifecycleOutboxEmitter;
    private final Clock clock;

    public ShipmentStatusTransitionService(
            ProcessGhnWebhookRepository processGhnWebhookRepository,
            OutboxEventRepository outboxEventRepository,
            ShipmentStatusChangedOutboxService shipmentStatusChangedOutboxService,
            ShipmentLifecycleOutboxEmitter shipmentLifecycleOutboxEmitter,
            Clock clock
    ) {
        this.processGhnWebhookRepository = processGhnWebhookRepository;
        this.outboxEventRepository = outboxEventRepository;
        this.shipmentStatusChangedOutboxService = shipmentStatusChangedOutboxService;
        this.shipmentLifecycleOutboxEmitter = shipmentLifecycleOutboxEmitter;
        this.clock = clock;
    }

    public ShipmentStatusTransitionResult apply(
            SellerShipmentRecord shipment,
            ShipmentStatus newStatus,
            String rawStatus,
            String trackingNumber
    ) {
        if (shipment.status() == newStatus) {
            return ShipmentStatusTransitionResult.unchanged(shipment);
        }

        Instant occurredAt = clock.instant();
        boolean updated = processGhnWebhookRepository.updateStatus(
                shipment.shipmentId(),
                shipment.status(),
                newStatus,
                occurredAt
        );
        if (!updated) {
            return ShipmentStatusTransitionResult.unchanged(shipment);
        }

        processGhnWebhookRepository.insertStatusHistory(
                shipment.shipmentId(),
                shipment.status(),
                newStatus,
                rawStatus,
                occurredAt
        );

        int orderItemsUpdated = AdminShipmentStatusOverridePolicy.orderItemStatusForCarrier(
                        shipment.carrier(),
                        newStatus
                )
                .map(itemStatus -> processGhnWebhookRepository.updateOrderItemsForShipment(
                        shipment.shipmentId(),
                        itemStatus.name(),
                        occurredAt
                ))
                .orElse(0);

        outboxEventRepository.save(shipmentStatusChangedOutboxService.build(
                shipment.shipmentId(),
                shipment.orderId(),
                shipment.sellerId(),
                shipment.status(),
                newStatus,
                occurredAt
        ));

        shipmentLifecycleOutboxEmitter.emitDedicatedNotificationEvents(
                shipment,
                newStatus,
                occurredAt,
                trackingNumber
        );

        return ShipmentStatusTransitionResult.applied(shipment, newStatus, orderItemsUpdated);
    }
}
