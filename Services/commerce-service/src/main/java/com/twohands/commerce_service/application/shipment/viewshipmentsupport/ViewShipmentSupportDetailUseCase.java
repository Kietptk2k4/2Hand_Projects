package com.twohands.commerce_service.application.shipment.viewshipmentsupport;

import com.twohands.commerce_service.domain.shipment.ShipmentCarrierStatusResolver;
import com.twohands.commerce_service.domain.shipment.ShipmentSupportDetailSnapshot;
import com.twohands.commerce_service.domain.shipment.ViewShipmentSupportDetailRepository;
import com.twohands.commerce_service.domain.shipment.ViewShipmentSupportDetailResult;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ViewShipmentSupportDetailUseCase {

    private final ViewShipmentSupportDetailRepository viewShipmentSupportDetailRepository;

    public ViewShipmentSupportDetailUseCase(ViewShipmentSupportDetailRepository viewShipmentSupportDetailRepository) {
        this.viewShipmentSupportDetailRepository = viewShipmentSupportDetailRepository;
    }

    @Transactional(readOnly = true)
    public ViewShipmentSupportDetailResult execute(ViewShipmentSupportDetailCommand command) {
        ShipmentSupportDetailSnapshot snapshot = viewShipmentSupportDetailRepository
                .findByShipmentId(command.shipmentId())
                .orElseThrow(() -> new AppException(ErrorCode.SHIPMENT_NOT_FOUND));

        String carrierStatus = ShipmentCarrierStatusResolver.resolve(
                snapshot.carrierWebhookEvents(),
                snapshot.statusHistory()
        );

        return new ViewShipmentSupportDetailResult(
                snapshot.shipment(),
                snapshot.buyerId(),
                snapshot.orderStatus(),
                carrierStatus,
                snapshot.shippingAddress(),
                snapshot.orderItems(),
                snapshot.statusHistory(),
                snapshot.carrierWebhookEvents()
        );
    }

    public String successMessage() {
        return "Shipment support detail retrieved successfully";
    }
}
