package com.twohands.commerce_service.application.shipment.common;

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
import java.util.Optional;

@Service
public class GhnShipmentStatusUpdateService {

    private static final Logger log = LoggerFactory.getLogger(GhnShipmentStatusUpdateService.class);

    private final ProcessGhnWebhookRepository processGhnWebhookRepository;
    private final ShipmentStatusTransitionService shipmentStatusTransitionService;
    private final Clock clock;

    public GhnShipmentStatusUpdateService(
            ProcessGhnWebhookRepository processGhnWebhookRepository,
            ShipmentStatusTransitionService shipmentStatusTransitionService,
            Clock clock
    ) {
        this.processGhnWebhookRepository = processGhnWebhookRepository;
        this.shipmentStatusTransitionService = shipmentStatusTransitionService;
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

        String lifecycleTracking = StringUtils.hasText(trackingNumber) ? trackingNumber.trim() : shipment.trackingNumber();
        ShipmentStatusTransitionResult transitionResult = shipmentStatusTransitionService.apply(
                shipment,
                newStatus,
                rawStatus,
                lifecycleTracking
        );

        if (!transitionResult.applied()) {
            return GhnShipmentStatusUpdateResult.unchanged(shipment.shipmentId(), shipment.status());
        }

        return GhnShipmentStatusUpdateResult.updated(
                shipment.shipmentId(),
                transitionResult.previousStatus(),
                transitionResult.currentStatus()
        );
    }
}
