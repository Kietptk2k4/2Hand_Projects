package com.twohands.commerce_service.application.shipment.viewsellershipment;

import com.twohands.commerce_service.domain.shipment.ManageSellerShipmentRepository;
import com.twohands.commerce_service.domain.shipment.SellerShipmentDetail;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class ViewSellerShipmentUseCase {

    private final ManageSellerShipmentRepository manageSellerShipmentRepository;

    public ViewSellerShipmentUseCase(ManageSellerShipmentRepository manageSellerShipmentRepository) {
        this.manageSellerShipmentRepository = manageSellerShipmentRepository;
    }

    public SellerShipmentDetail execute(UUID sellerId, UUID shipmentId) {
        return manageSellerShipmentRepository.findDetailForSeller(shipmentId, sellerId)
                .orElseThrow(() -> new AppException(ErrorCode.SHIPMENT_NOT_FOUND));
    }
}
