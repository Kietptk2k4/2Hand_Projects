package com.twohands.commerce_service.application.shipment.updatesellershipment;

import com.twohands.commerce_service.application.shipment.common.SellerShipmentBuyerEnrichmentService;
import com.twohands.commerce_service.domain.shipment.ManageSellerShipmentRepository;
import com.twohands.commerce_service.domain.shipment.ManualShipmentStatusPolicy;
import com.twohands.commerce_service.domain.shipment.SellerShipmentDetail;
import com.twohands.commerce_service.domain.shipment.SellerShipmentRecord;
import com.twohands.commerce_service.domain.shipment.ShipmentStatus;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Clock;
import java.time.Instant;

@Service
public class UpdateSellerShipmentUseCase {

    private final ManageSellerShipmentRepository manageSellerShipmentRepository;
    private final UpdateSellerShipmentTransactionService updateSellerShipmentTransactionService;
    private final SellerShipmentBuyerEnrichmentService sellerShipmentBuyerEnrichmentService;
    private final Clock clock;

    public UpdateSellerShipmentUseCase(
            ManageSellerShipmentRepository manageSellerShipmentRepository,
            UpdateSellerShipmentTransactionService updateSellerShipmentTransactionService,
            SellerShipmentBuyerEnrichmentService sellerShipmentBuyerEnrichmentService,
            Clock clock
    ) {
        this.manageSellerShipmentRepository = manageSellerShipmentRepository;
        this.updateSellerShipmentTransactionService = updateSellerShipmentTransactionService;
        this.sellerShipmentBuyerEnrichmentService = sellerShipmentBuyerEnrichmentService;
        this.clock = clock;
    }

    public SellerShipmentDetail execute(UpdateSellerShipmentCommand command) {
        if (!StringUtils.hasText(command.status()) && !StringUtils.hasText(command.trackingNumber())) {
            throw new AppException(ErrorCode.BAD_REQUEST, "status or tracking_number is required");
        }

        SellerShipmentRecord current = manageSellerShipmentRepository.findShipmentForSeller(
                        command.shipmentId(),
                        command.sellerId()
                )
                .orElseThrow(() -> new AppException(ErrorCode.SHIPMENT_NOT_FOUND));

        if (!ManualShipmentStatusPolicy.isEditableCarrier(current.carrier())) {
            throw new AppException(
                    ErrorCode.SHIPMENT_CARRIER_NOT_EDITABLE,
                    "Only MANUAL or SELF_DELIVERY shipments can be updated by seller"
            );
        }

        Instant occurredAt = clock.instant();
        ShipmentStatus newStatus = parseStatus(command.status());
        String trackingNumber = normalizeTracking(command.trackingNumber());

        if (newStatus == null) {
            return applyTrackingOnly(current, trackingNumber, occurredAt);
        }

        return applyStatusChange(current, newStatus, trackingNumber, occurredAt);
    }

    public String successMessage() {
        return "Cap nhat shipment thanh cong.";
    }

    private SellerShipmentDetail applyTrackingOnly(
            SellerShipmentRecord current,
            String trackingNumber,
            Instant occurredAt
    ) {
        if (!StringUtils.hasText(trackingNumber)) {
            throw new AppException(ErrorCode.BAD_REQUEST, "tracking_number is required");
        }
        if (!ManualShipmentStatusPolicy.canEditTracking(current.status())) {
            throw new AppException(ErrorCode.INVALID_SHIPMENT_STATUS, "Tracking cannot be updated for this status");
        }

        SellerShipmentRecord updated = updateSellerShipmentTransactionService.applyTrackingOnly(
                current,
                trackingNumber,
                occurredAt
        );
        return loadDetail(updated.shipmentId(), updated.sellerId());
    }

    private SellerShipmentDetail applyStatusChange(
            SellerShipmentRecord current,
            ShipmentStatus newStatus,
            String trackingNumber,
            Instant occurredAt
    ) {
        if (!ManualShipmentStatusPolicy.canTransition(current.status(), newStatus)) {
            throw new AppException(
                    ErrorCode.INVALID_SHIPMENT_STATUS,
                    "Invalid shipment status transition from " + current.status() + " to " + newStatus
            );
        }

        if (newStatus == current.status()) {
            if (!StringUtils.hasText(trackingNumber)) {
                return loadDetail(current.shipmentId(), current.sellerId());
            }
            return applyTrackingOnly(current, trackingNumber, occurredAt);
        }

        if (StringUtils.hasText(trackingNumber) && !ManualShipmentStatusPolicy.canEditTracking(current.status())) {
            throw new AppException(ErrorCode.INVALID_SHIPMENT_STATUS, "Tracking cannot be updated for this status");
        }

        SellerShipmentRecord updated = updateSellerShipmentTransactionService.applyStatusChange(
                current,
                newStatus,
                trackingNumber,
                occurredAt
        );
        return loadDetail(updated.shipmentId(), updated.sellerId());
    }

    private SellerShipmentDetail loadDetail(java.util.UUID shipmentId, java.util.UUID sellerId) {
        SellerShipmentDetail detail = manageSellerShipmentRepository.findDetailForSeller(shipmentId, sellerId)
                .orElseThrow(() -> new AppException(ErrorCode.SHIPMENT_NOT_FOUND));
        return sellerShipmentBuyerEnrichmentService.enrich(detail);
    }

    private ShipmentStatus parseStatus(String raw) {
        if (!StringUtils.hasText(raw)) {
            return null;
        }
        try {
            return ShipmentStatus.valueOf(raw.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new AppException(ErrorCode.BAD_REQUEST, "Invalid shipment status: " + raw);
        }
    }

    private String normalizeTracking(String raw) {
        if (!StringUtils.hasText(raw)) {
            return null;
        }
        return raw.trim();
    }
}
