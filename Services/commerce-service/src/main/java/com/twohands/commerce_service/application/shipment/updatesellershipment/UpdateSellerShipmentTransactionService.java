package com.twohands.commerce_service.application.shipment.updatesellershipment;

import com.twohands.commerce_service.application.shipment.common.ShipmentLifecycleOutboxEmitter;
import com.twohands.commerce_service.application.shipment.common.ShipmentStatusChangedOutboxService;
import com.twohands.commerce_service.domain.outbox.OutboxEventRepository;
import com.twohands.commerce_service.domain.shipment.ManageSellerShipmentRepository;
import com.twohands.commerce_service.domain.shipment.ManualShipmentStatusPolicy;
import com.twohands.commerce_service.domain.shipment.SellerShipmentRecord;
import com.twohands.commerce_service.domain.shipment.ShipmentStatus;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class UpdateSellerShipmentTransactionService {

    private final ManageSellerShipmentRepository manageSellerShipmentRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final ShipmentStatusChangedOutboxService shipmentStatusChangedOutboxService;
    private final ShipmentLifecycleOutboxEmitter shipmentLifecycleOutboxEmitter;

    public UpdateSellerShipmentTransactionService(
            ManageSellerShipmentRepository manageSellerShipmentRepository,
            OutboxEventRepository outboxEventRepository,
            ShipmentStatusChangedOutboxService shipmentStatusChangedOutboxService,
            ShipmentLifecycleOutboxEmitter shipmentLifecycleOutboxEmitter
    ) {
        this.manageSellerShipmentRepository = manageSellerShipmentRepository;
        this.outboxEventRepository = outboxEventRepository;
        this.shipmentStatusChangedOutboxService = shipmentStatusChangedOutboxService;
        this.shipmentLifecycleOutboxEmitter = shipmentLifecycleOutboxEmitter;
    }

    @Transactional
    public SellerShipmentRecord applyStatusChange(
            SellerShipmentRecord current,
            ShipmentStatus newStatus,
            String trackingNumber,
            Instant occurredAt
    ) {
        boolean updated = manageSellerShipmentRepository.updateStatusAndTracking(
                current.shipmentId(),
                current.sellerId(),
                current.status(),
                newStatus,
                trackingNumber,
                occurredAt
        );
        if (!updated) {
            throw new AppException(ErrorCode.INVALID_SHIPMENT_STATUS, "Shipment status changed concurrently");
        }

        manageSellerShipmentRepository.insertStatusHistory(
                current.shipmentId(),
                current.status(),
                newStatus,
                occurredAt
        );

        ManualShipmentStatusPolicy.orderItemStatusForShipmentStatus(newStatus)
                .ifPresent(itemStatus -> manageSellerShipmentRepository.updateOrderItemsForShipment(
                        current.shipmentId(),
                        itemStatus.name(),
                        occurredAt
                ));

        outboxEventRepository.save(shipmentStatusChangedOutboxService.build(
                current.shipmentId(),
                current.orderId(),
                current.sellerId(),
                current.status(),
                newStatus,
                occurredAt
        ));

        shipmentLifecycleOutboxEmitter.emitDedicatedNotificationEvents(
                current,
                newStatus,
                occurredAt,
                trackingNumber
        );

        return reload(current.shipmentId(), current.sellerId());
    }

    @Transactional
    public SellerShipmentRecord applyTrackingOnly(
            SellerShipmentRecord current,
            String trackingNumber,
            Instant occurredAt
    ) {
        boolean updated = manageSellerShipmentRepository.updateTrackingOnly(
                current.shipmentId(),
                current.sellerId(),
                trackingNumber,
                occurredAt
        );
        if (!updated) {
            throw new AppException(ErrorCode.INVALID_SHIPMENT_STATUS, "Shipment cannot be updated");
        }
        return reload(current.shipmentId(), current.sellerId());
    }

    private SellerShipmentRecord reload(UUID shipmentId, UUID sellerId) {
        return manageSellerShipmentRepository.findShipmentForSeller(shipmentId, sellerId)
                .orElseThrow(() -> new AppException(ErrorCode.SHIPMENT_NOT_FOUND));
    }
}
