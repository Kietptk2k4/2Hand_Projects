package com.twohands.commerce_service.application.shipment.common;

import com.twohands.commerce_service.application.review.common.ReviewBuyerEnrichmentService;
import com.twohands.commerce_service.domain.order.CommerceBuyerSummary;
import com.twohands.commerce_service.domain.shipment.SellerShipmentDetail;
import org.springframework.stereotype.Service;

@Service
public class SellerShipmentBuyerEnrichmentService {

    private final ReviewBuyerEnrichmentService reviewBuyerEnrichmentService;

    public SellerShipmentBuyerEnrichmentService(ReviewBuyerEnrichmentService reviewBuyerEnrichmentService) {
        this.reviewBuyerEnrichmentService = reviewBuyerEnrichmentService;
    }

    public SellerShipmentDetail enrich(SellerShipmentDetail detail) {
        CommerceBuyerSummary buyer = reviewBuyerEnrichmentService.enrichBuyer(
                detail.buyer() == null ? null : detail.buyer().buyerId()
        );
        return new SellerShipmentDetail(
                detail.shipment(),
                detail.addressSnapshot(),
                detail.orderItems(),
                buyer
        );
    }
}
