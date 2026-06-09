package com.twohands.commerce_service.application.shipment.common;

import com.twohands.commerce_service.domain.outbox.OutboxEventRepository;
import com.twohands.commerce_service.domain.shipment.GhnShipmentStatusMapper;
import com.twohands.commerce_service.domain.shipment.GhnShipmentStatusPolicy;
import com.twohands.commerce_service.domain.shipment.ProcessGhnWebhookRepository;
import com.twohands.commerce_service.domain.shipment.SellerShipmentRecord;
import com.twohands.commerce_service.domain.shipment.ShipmentStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Clock;
import java.time.Instant;
import java.util.Optional;

@Service
public class GhnShipmentStatusUpdateService {

    private static final Logger log = LoggerFactory.getLogger(GhnShipmentStatusUpdateService.class);

    private final ProcessGhnWebhookRepository processGhnWebhookRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final ShipmentStatusChangedOutboxService shipmentStatusChangedOutboxService;
    private final ShipmentLifecycleOutboxEmitter shipmentLifecycleOutboxEmitter;
    private final Clock clock;

    public GhnShipmentStatusUpdateService(
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

    public GhnShipmentStatusUpdateResult apply(
            SellerShipmentRecord shipment,
            String rawStatus,
            String trackingNumber
    ) {
        Optional<ShipmentStatus> mappedStatus = GhnShipmentStatusMapper.map(rawStatus);
        if (mappedStatus.isEmpty()) {
            log.warn("GHN status '{}' is unmapped for shipment {}", rawStatus, shipment.shipmentId());
            return GhnShipmentStatusUpdateResult.unmapped(shipment.shipmentId(), shipment.status());
        }

        ShipmentStatus newStatus = mappedStatus.get();
        if (StringUtils.hasText(trackingNumber) && !StringUtils.hasText(shipment.trackingNumber())) {
            processGhnWebhookRepository.updateTrackingNumberIfBlank(
                    shipment.shipmentId(),
                    trackingNumber.trim(),
                    clock.instant()
            );
        }

        if (shipment.status() == newStatus) {
            return GhnShipmentStatusUpdateResult.unchanged(shipment.shipmentId(), shipment.status());
        }

        if (!GhnShipmentStatusPolicy.canTransition(shipment.status(), newStatus)) {
            log.warn(
                    "GHN ignored out-of-order transition {} -> {} for shipment {}",
                    shipment.status(),
                    newStatus,
                    shipment.shipmentId()
            );
            return GhnShipmentStatusUpdateResult.ignored(shipment.shipmentId(), shipment.status(), newStatus);
        }

        Instant occurredAt = clock.instant();
        boolean updated = processGhnWebhookRepository.updateStatus(
                shipment.shipmentId(),
                shipment.status(),
                newStatus,
                occurredAt
        );
        if (!updated) {
            return GhnShipmentStatusUpdateResult.unchanged(shipment.shipmentId(), shipment.status());
        }

        processGhnWebhookRepository.insertStatusHistory(
                shipment.shipmentId(),
                shipment.status(),
                newStatus,
                rawStatus,
                occurredAt
        );

        GhnShipmentStatusPolicy.orderItemStatusForShipmentStatus(newStatus)
                .ifPresent(itemStatus -> processGhnWebhookRepository.updateOrderItemsForShipment(
                        shipment.shipmentId(),
                        itemStatus.name(),
                        occurredAt
                ));

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

        return GhnShipmentStatusUpdateResult.updated(shipment.shipmentId(), shipment.status(), newStatus);
    }
}
