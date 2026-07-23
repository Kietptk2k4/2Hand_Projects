package com.twohands.commerce_service.application.admin.viewshopdetailformoderation;

import com.twohands.commerce_service.domain.admin.AdminShopDetailEntry;
import com.twohands.commerce_service.domain.admin.ViewShopDetailForModerationRepository;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ViewShopDetailForModerationUseCase {

    private static final String SUCCESS_MESSAGE = "Lay chi tiet shop kiem duyet thanh cong.";

    private final ViewShopDetailForModerationRepository viewShopDetailForModerationRepository;

    public ViewShopDetailForModerationUseCase(
            ViewShopDetailForModerationRepository viewShopDetailForModerationRepository
    ) {
        this.viewShopDetailForModerationRepository = viewShopDetailForModerationRepository;
    }

    @Transactional(readOnly = true)
    public ViewShopDetailForModerationResult execute(ViewShopDetailForModerationCommand command) {
        AdminShopDetailEntry entry = viewShopDetailForModerationRepository.findByShopId(command.shopId())
                .orElseThrow(() -> new AppException(ErrorCode.SHOP_NOT_FOUND));

        return new ViewShopDetailForModerationResult(
                entry.shopId(),
                entry.sellerId(),
                entry.shopName(),
                entry.description(),
                entry.logoUrl(),
                entry.status(),
                entry.createdAt(),
                entry.updatedAt(),
                entry.totalProductCount(),
                entry.activeProductCount(),
                entry.openOrderCount()
        );
    }

    public String successMessage() {
        return SUCCESS_MESSAGE;
    }
}
