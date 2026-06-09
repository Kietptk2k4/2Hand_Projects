package com.twohands.commerce_service.application.shipment.cancelghnshipment;

import com.twohands.commerce_service.application.shipment.common.GhnShipmentStatusUpdateService;
import com.twohands.commerce_service.domain.shipment.GhnCancelOrderGateway;
import com.twohands.commerce_service.domain.shipment.ManageSellerShipmentRepository;
import com.twohands.commerce_service.domain.shipment.SellerShipmentDetail;
import com.twohands.commerce_service.domain.shipment.SellerShipmentRecord;
import com.twohands.commerce_service.domain.shipment.ShipmentCarrier;
import com.twohands.commerce_service.domain.shipment.ShipmentStatus;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.EnumSet;
import java.util.UUID;

@Service
public class CancelGhnShipmentUseCase {

    private static final EnumSet<ShipmentStatus> CANCELLABLE_STATUSES = EnumSet.of(
            ShipmentStatus.PENDING,
            ShipmentStatus.PICKING_UP,
            ShipmentStatus.READY_TO_SHIP
    );

    private final ManageSellerShipmentRepository manageSellerShipmentRepository;
    private final GhnCancelOrderGateway ghnCancelOrderGateway;
    private final GhnShipmentStatusUpdateService ghnShipmentStatusUpdateService;

    public CancelGhnShipmentUseCase(
            ManageSellerShipmentRepository manageSellerShipmentRepository,
            GhnCancelOrderGateway ghnCancelOrderGateway,
            GhnShipmentStatusUpdateService ghnShipmentStatusUpdateService
    ) {
        this.manageSellerShipmentRepository = manageSellerShipmentRepository;
        this.ghnCancelOrderGateway = ghnCancelOrderGateway;
        this.ghnShipmentStatusUpdateService = ghnShipmentStatusUpdateService;
    }

    @Transactional
    public SellerShipmentDetail execute(CancelGhnShipmentCommand command) {
        SellerShipmentRecord shipment = manageSellerShipmentRepository.findShipmentForSeller(
                        command.shipmentId(),
                        command.sellerId()
                )
                .orElseThrow(() -> new AppException(ErrorCode.SHIPMENT_NOT_FOUND));

        if (shipment.carrier() != ShipmentCarrier.GHN) {
            throw new AppException(ErrorCode.INVALID_SHIPMENT_CARRIER, "Shipment is not a GHN carrier");
        }
        if (!StringUtils.hasText(shipment.ghnOrderCode())) {
            throw new AppException(ErrorCode.BAD_REQUEST, "Shipment has no GHN order code to cancel");
        }
        if (!CANCELLABLE_STATUSES.contains(shipment.status())) {
            throw new AppException(
                    ErrorCode.INVALID_SHIPMENT_STATUS,
                    "GHN shipment cannot be cancelled in status " + shipment.status()
            );
        }

        ghnCancelOrderGateway.cancelOrder(shipment.ghnOrderCode());
        ghnShipmentStatusUpdateService.apply(shipment, "cancel", shipment.ghnOrderCode());

        return manageSellerShipmentRepository.findDetailForSeller(command.shipmentId(), command.sellerId())
                .orElseThrow(() -> new AppException(ErrorCode.SHIPMENT_NOT_FOUND));
    }

    public String successMessage() {
        return "Huy van don GHN thanh cong.";
    }

    public record CancelGhnShipmentCommand(UUID sellerId, UUID shipmentId) {
    }
}
