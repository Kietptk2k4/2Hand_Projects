package com.twohands.commerce_service.application.product.viewsellerproductdetail;

import com.twohands.commerce_service.domain.product.SellerProductDetail;
import com.twohands.commerce_service.domain.product.ViewSellerProductCatalogRepository;
import com.twohands.commerce_service.domain.shop.SellerShopRepository;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;

@Service
public class ViewSellerProductDetailUseCase {

    private final SellerShopRepository sellerShopRepository;
    private final ViewSellerProductCatalogRepository viewSellerProductCatalogRepository;
    private final Clock clock;

    public ViewSellerProductDetailUseCase(
            SellerShopRepository sellerShopRepository,
            ViewSellerProductCatalogRepository viewSellerProductCatalogRepository,
            Clock clock
    ) {
        this.sellerShopRepository = sellerShopRepository;
        this.viewSellerProductCatalogRepository = viewSellerProductCatalogRepository;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public SellerProductDetail execute(ViewSellerProductDetailCommand command) {
        sellerShopRepository.findBySellerId(command.sellerId())
                .orElseThrow(() -> new AppException(ErrorCode.SELLER_SHOP_NOT_FOUND));

        return viewSellerProductCatalogRepository
                .findDetailBySellerId(command.sellerId(), command.productId(), clock.instant())
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));
    }

    public String successMessage() {
        return "Lay chi tiet san pham thanh cong.";
    }
}
