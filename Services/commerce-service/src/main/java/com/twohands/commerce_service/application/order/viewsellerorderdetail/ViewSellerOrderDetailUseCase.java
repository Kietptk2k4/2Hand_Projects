package com.twohands.commerce_service.application.order.viewsellerorderdetail;

import com.twohands.commerce_service.application.review.common.ReviewBuyerEnrichmentService;
import com.twohands.commerce_service.domain.order.CommerceBuyerSummary;
import com.twohands.commerce_service.domain.order.ViewSellerOrderDetailRepository;
import com.twohands.commerce_service.domain.order.ViewSellerOrderDetailResult;
import com.twohands.commerce_service.domain.shop.SellerShopRepository;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ViewSellerOrderDetailUseCase {

    private final SellerShopRepository sellerShopRepository;
    private final ViewSellerOrderDetailRepository viewSellerOrderDetailRepository;
    private final ReviewBuyerEnrichmentService reviewBuyerEnrichmentService;

    public ViewSellerOrderDetailUseCase(
            SellerShopRepository sellerShopRepository,
            ViewSellerOrderDetailRepository viewSellerOrderDetailRepository,
            ReviewBuyerEnrichmentService reviewBuyerEnrichmentService
    ) {
        this.sellerShopRepository = sellerShopRepository;
        this.viewSellerOrderDetailRepository = viewSellerOrderDetailRepository;
        this.reviewBuyerEnrichmentService = reviewBuyerEnrichmentService;
    }

    @Transactional(readOnly = true)
    public ViewSellerOrderDetailResult execute(ViewSellerOrderDetailCommand command) {
        sellerShopRepository.findBySellerId(command.sellerId())
                .orElseThrow(() -> new AppException(ErrorCode.SELLER_SHOP_NOT_FOUND));

        ViewSellerOrderDetailResult result = viewSellerOrderDetailRepository
                .findSellerOrderDetail(command.sellerId(), command.orderId())
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        CommerceBuyerSummary buyer = reviewBuyerEnrichmentService.enrichBuyer(
                result.buyer() == null ? null : result.buyer().buyerId()
        );

        return new ViewSellerOrderDetailResult(
                result.orderId(),
                result.orderStatus(),
                result.orderPaymentStatus(),
                result.orderPaymentMethod(),
                result.orderCreatedAt(),
                result.payment(),
                result.sellerItemsSubtotal(),
                result.sellerShippingTotal(),
                result.items(),
                result.shippingAddress(),
                buyer,
                result.activeRefundRequest(),
                result.cancellationNote()
        );
    }

    public String successMessage() {
        return "Lay chi tiet don hang seller thanh cong.";
    }
}
