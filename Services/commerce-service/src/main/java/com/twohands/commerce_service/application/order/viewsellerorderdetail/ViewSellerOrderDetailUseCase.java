package com.twohands.commerce_service.application.order.viewsellerorderdetail;

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

    public ViewSellerOrderDetailUseCase(
            SellerShopRepository sellerShopRepository,
            ViewSellerOrderDetailRepository viewSellerOrderDetailRepository
    ) {
        this.sellerShopRepository = sellerShopRepository;
        this.viewSellerOrderDetailRepository = viewSellerOrderDetailRepository;
    }

    @Transactional(readOnly = true)
    public ViewSellerOrderDetailResult execute(ViewSellerOrderDetailCommand command) {
        sellerShopRepository.findBySellerId(command.sellerId())
                .orElseThrow(() -> new AppException(ErrorCode.SELLER_SHOP_NOT_FOUND));

        return viewSellerOrderDetailRepository
                .findSellerOrderDetail(command.sellerId(), command.orderId())
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));
    }

    public String successMessage() {
        return "Lay chi tiet don hang seller thanh cong.";
    }
}
