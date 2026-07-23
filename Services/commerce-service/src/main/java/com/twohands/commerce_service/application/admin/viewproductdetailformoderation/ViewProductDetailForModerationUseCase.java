package com.twohands.commerce_service.application.admin.viewproductdetailformoderation;

import com.twohands.commerce_service.domain.admin.AdminProductDetailEntry;
import com.twohands.commerce_service.domain.admin.ViewProductDetailForModerationRepository;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ViewProductDetailForModerationUseCase {

    private static final String SUCCESS_MESSAGE = "Lay chi tiet san pham kiem duyet thanh cong.";

    private final ViewProductDetailForModerationRepository viewProductDetailForModerationRepository;

    public ViewProductDetailForModerationUseCase(
            ViewProductDetailForModerationRepository viewProductDetailForModerationRepository
    ) {
        this.viewProductDetailForModerationRepository = viewProductDetailForModerationRepository;
    }

    @Transactional(readOnly = true)
    public ViewProductDetailForModerationResult execute(ViewProductDetailForModerationCommand command) {
        AdminProductDetailEntry entry = viewProductDetailForModerationRepository
                .findByProductId(command.productId())
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));

        return new ViewProductDetailForModerationResult(
                entry.productId(),
                entry.sellerId(),
                entry.shopId(),
                entry.shopName(),
                entry.title(),
                entry.description(),
                entry.status(),
                entry.categoryId(),
                entry.categoryName(),
                entry.price(),
                entry.effectivePrice(),
                entry.stockQuantity(),
                entry.createdAt(),
                entry.updatedAt(),
                entry.removedAt(),
                entry.removeReason(),
                entry.openOrderCount(),
                entry.media(),
                entry.attributes()
        );
    }

    public String successMessage() {
        return SUCCESS_MESSAGE;
    }
}
