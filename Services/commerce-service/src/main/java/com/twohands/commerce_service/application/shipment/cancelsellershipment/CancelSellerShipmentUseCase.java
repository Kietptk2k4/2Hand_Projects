package com.twohands.commerce_service.application.shipment.cancelsellershipment;

import com.twohands.commerce_service.application.shipment.common.GhnShipmentStatusUpdateService;
import com.twohands.commerce_service.application.shipment.common.SellerShipmentBuyerEnrichmentService;
import com.twohands.commerce_service.application.shipment.common.ShipmentStatusTransitionService;
import com.twohands.commerce_service.domain.shipment.AdminShipmentStatusOverridePolicy;
import com.twohands.commerce_service.domain.shipment.GhnCancelOrderGateway;
import com.twohands.commerce_service.domain.shipment.ManageSellerShipmentRepository;
import com.twohands.commerce_service.domain.shipment.SellerShipmentCancellationPolicy;
import com.twohands.commerce_service.domain.shipment.SellerShipmentDetail;
import com.twohands.commerce_service.domain.shipment.SellerShipmentRecord;
import com.twohands.commerce_service.domain.shipment.ShipmentCarrier;
import com.twohands.commerce_service.domain.shipment.ShipmentStatus;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.UUID;

@Service
public class CancelSellerShipmentUseCase {

    private final ManageSellerShipmentRepository manageSellerShipmentRepository;
    private final GhnCancelOrderGateway ghnCancelOrderGateway;
    private final GhnShipmentStatusUpdateService ghnShipmentStatusUpdateService;
    private final ShipmentStatusTransitionService shipmentStatusTransitionService;
    private final SellerShipmentBuyerEnrichmentService sellerShipmentBuyerEnrichmentService;

    public CancelSellerShipmentUseCase(
            ManageSellerShipmentRepository manageSellerShipmentRepository,
            GhnCancelOrderGateway ghnCancelOrderGateway,
            GhnShipmentStatusUpdateService ghnShipmentStatusUpdateService,
            ShipmentStatusTransitionService shipmentStatusTransitionService,
            SellerShipmentBuyerEnrichmentService sellerShipmentBuyerEnrichmentService
    ) {
        this.manageSellerShipmentRepository = manageSellerShipmentRepository;
        this.ghnCancelOrderGateway = ghnCancelOrderGateway;
        this.ghnShipmentStatusUpdateService = ghnShipmentStatusUpdateService;
        this.shipmentStatusTransitionService = shipmentStatusTransitionService;
        this.sellerShipmentBuyerEnrichmentService = sellerShipmentBuyerEnrichmentService;
    }

    @Transactional
    public SellerShipmentDetail execute(CancelSellerShipmentCommand command) {
        SellerShipmentRecord shipment = manageSellerShipmentRepository.findShipmentForSeller(
                        command.shipmentId(),
                        command.sellerId()
                )
                .orElseThrow(() -> new AppException(ErrorCode.SHIPMENT_NOT_FOUND));

        if (!SellerShipmentCancellationPolicy.canCancel(shipment.carrier(), shipment.status())) {
            throw new AppException(
                    ErrorCode.INVALID_SHIPMENT_STATUS,
                    "Shipment cannot be cancelled in status " + shipment.status()
            );
        }

        if (shipment.carrier() == ShipmentCarrier.GHN) {
            cancelGhnShipment(shipment);
        } else {
            cancelManualShipment(shipment);
        }

        SellerShipmentDetail detail = manageSellerShipmentRepository.findDetailForSeller(
                        command.shipmentId(),
                        command.sellerId()
                )
                .orElseThrow(() -> new AppException(ErrorCode.SHIPMENT_NOT_FOUND));
        return sellerShipmentBuyerEnrichmentService.enrich(detail);
    }

    public String successMessage() {
        return "Huy van don thanh cong.";
    }

    private void cancelGhnShipment(SellerShipmentRecord shipment) {
        if (!StringUtils.hasText(shipment.ghnOrderCode())) {
            throw new AppException(ErrorCode.BAD_REQUEST, "Shipment has no GHN order code to cancel");
        }
        ghnCancelOrderGateway.cancelOrder(shipment.ghnOrderCode());
        ghnShipmentStatusUpdateService.apply(shipment, "cancel", shipment.ghnOrderCode());
    }

    private void cancelManualShipment(SellerShipmentRecord shipment) {
        if (!AdminShipmentStatusOverridePolicy.canTransition(
                shipment.carrier(),
                shipment.status(),
                ShipmentStatus.CANCELLED,
                false
        )) {
            throw new AppException(
                    ErrorCode.INVALID_SHIPMENT_STATUS,
                    "Shipment cannot be cancelled in status " + shipment.status()
            );
        }

        shipmentStatusTransitionService.apply(
                shipment,
                ShipmentStatus.CANCELLED,
                "seller_cancel",
                shipment.trackingNumber()
        );
    }

    public record CancelSellerShipmentCommand(UUID sellerId, UUID shipmentId) {
    }
}
