package com.twohands.commerce_service.application.shipment.adminoverrideshipmentstatus;

import com.twohands.commerce_service.application.shipment.common.ShipmentStatusTransitionResult;
import com.twohands.commerce_service.application.shipment.common.ShipmentStatusTransitionService;
import com.twohands.commerce_service.domain.shipment.AdminShipmentStatusOverridePolicy;
import com.twohands.commerce_service.domain.shipment.ProcessGhnWebhookRepository;
import com.twohands.commerce_service.domain.shipment.SellerShipmentRecord;
import com.twohands.commerce_service.domain.shipment.ShipmentStatus;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Clock;

@Service
public class AdminOverrideShipmentStatusUseCase {

    private final ProcessGhnWebhookRepository processGhnWebhookRepository;
    private final ShipmentStatusTransitionService shipmentStatusTransitionService;
    private final Clock clock;

    public AdminOverrideShipmentStatusUseCase(
            ProcessGhnWebhookRepository processGhnWebhookRepository,
            ShipmentStatusTransitionService shipmentStatusTransitionService,
            Clock clock
    ) {
        this.processGhnWebhookRepository = processGhnWebhookRepository;
        this.shipmentStatusTransitionService = shipmentStatusTransitionService;
        this.clock = clock;
    }

    @Transactional
    public AdminOverrideShipmentStatusResult execute(AdminOverrideShipmentStatusCommand command) {
        ShipmentStatus targetStatus = parseStatus(command.status());
        validateReason(command.reason());

        SellerShipmentRecord shipment = processGhnWebhookRepository.findByShipmentIdForUpdate(command.shipmentId())
                .orElseThrow(() -> new AppException(ErrorCode.SHIPMENT_NOT_FOUND));

        if (shipment.status() == targetStatus) {
            return AdminOverrideShipmentStatusResult.unchanged(
                    shipment.shipmentId(),
                    shipment.orderId(),
                    shipment.carrier(),
                    shipment.status()
            );
        }

        if (!AdminShipmentStatusOverridePolicy.canTransition(
                shipment.carrier(),
                shipment.status(),
                targetStatus,
                command.force()
        )) {
            throw new AppException(
                    ErrorCode.INVALID_SHIPMENT_STATUS,
                    "Invalid shipment status transition from " + shipment.status() + " to " + targetStatus
            );
        }

        ShipmentStatusTransitionResult transitionResult = shipmentStatusTransitionService.apply(
                shipment,
                targetStatus,
                AdminShipmentStatusOverridePolicy.RAW_STATUS,
                shipment.trackingNumber()
        );

        if (!transitionResult.applied()) {
            return AdminOverrideShipmentStatusResult.unchanged(
                    shipment.shipmentId(),
                    shipment.orderId(),
                    shipment.carrier(),
                    shipment.status()
            );
        }

        return new AdminOverrideShipmentStatusResult(
                transitionResult.shipmentId(),
                shipment.orderId(),
                shipment.carrier(),
                transitionResult.previousStatus(),
                transitionResult.currentStatus(),
                transitionResult.orderItemsUpdated(),
                clock.instant(),
                true
        );
    }

    public String successMessage(AdminOverrideShipmentStatusResult result) {
        return result.applied()
                ? "Shipment status overridden successfully"
                : "Shipment status unchanged";
    }

    private ShipmentStatus parseStatus(String raw) {
        if (!StringUtils.hasText(raw)) {
            throw new AppException(ErrorCode.BAD_REQUEST, "status is required");
        }
        try {
            return ShipmentStatus.valueOf(raw.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new AppException(ErrorCode.BAD_REQUEST, "Invalid shipment status: " + raw);
        }
    }

    private void validateReason(String reason) {
        if (!StringUtils.hasText(reason)) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "reason is required");
        }
        String trimmed = reason.trim();
        if (trimmed.length() < AdminShipmentStatusOverridePolicy.REASON_MIN_LENGTH) {
            throw new AppException(
                    ErrorCode.VALIDATION_ERROR,
                    "reason must be at least " + AdminShipmentStatusOverridePolicy.REASON_MIN_LENGTH + " characters"
            );
        }
        if (trimmed.length() > AdminShipmentStatusOverridePolicy.REASON_MAX_LENGTH) {
            throw new AppException(
                    ErrorCode.VALIDATION_ERROR,
                    "reason must be at most " + AdminShipmentStatusOverridePolicy.REASON_MAX_LENGTH + " characters"
            );
        }
    }
}
