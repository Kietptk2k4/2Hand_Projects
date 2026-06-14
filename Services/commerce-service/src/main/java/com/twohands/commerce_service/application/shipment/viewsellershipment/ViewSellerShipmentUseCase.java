package com.twohands.commerce_service.application.shipment.viewsellershipment;

import com.twohands.commerce_service.application.shipment.common.SellerShipmentBuyerEnrichmentService;
import com.twohands.commerce_service.domain.shipment.ManageSellerShipmentRepository;
import com.twohands.commerce_service.domain.shipment.SellerShipmentDetail;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class ViewSellerShipmentUseCase {

    private final ManageSellerShipmentRepository manageSellerShipmentRepository;
    private final SellerShipmentBuyerEnrichmentService sellerShipmentBuyerEnrichmentService;

    public ViewSellerShipmentUseCase(
            ManageSellerShipmentRepository manageSellerShipmentRepository,
            SellerShipmentBuyerEnrichmentService sellerShipmentBuyerEnrichmentService
    ) {
        this.manageSellerShipmentRepository = manageSellerShipmentRepository;
        this.sellerShipmentBuyerEnrichmentService = sellerShipmentBuyerEnrichmentService;
    }

    public SellerShipmentDetail execute(UUID sellerId, UUID shipmentId) {
        SellerShipmentDetail detail = manageSellerShipmentRepository.findDetailForSeller(shipmentId, sellerId)
                .orElseThrow(() -> new AppException(ErrorCode.SHIPMENT_NOT_FOUND));
        return sellerShipmentBuyerEnrichmentService.enrich(detail);
    }
}
