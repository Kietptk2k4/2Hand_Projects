package com.twohands.commerce_service.application.shop.viewmyshop;

import com.twohands.commerce_service.domain.shop.ViewMyShopRepository;
import com.twohands.commerce_service.domain.shop.ViewMyShopResult;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ViewMyShopUseCase {

    private final ViewMyShopRepository viewMyShopRepository;

    public ViewMyShopUseCase(ViewMyShopRepository viewMyShopRepository) {
        this.viewMyShopRepository = viewMyShopRepository;
    }

    @Transactional(readOnly = true)
    public ViewMyShopResult execute(ViewMyShopCommand command) {
        return viewMyShopRepository.findBySellerId(command.sellerId())
                .orElseThrow(() -> new AppException(ErrorCode.SHOP_NOT_FOUND));
    }

    public String successMessage() {
        return "Lay thong tin shop thanh cong.";
    }
}
