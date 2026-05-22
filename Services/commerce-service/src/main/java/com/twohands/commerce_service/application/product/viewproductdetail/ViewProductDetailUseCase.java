package com.twohands.commerce_service.application.product.viewproductdetail;

import com.twohands.commerce_service.domain.product.ViewProductDetailRepository;
import com.twohands.commerce_service.domain.product.ViewProductDetailResult;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.UUID;

@Service
public class ViewProductDetailUseCase {

    private final ViewProductDetailRepository viewProductDetailRepository;
    private final Clock clock;

    public ViewProductDetailUseCase(
            ViewProductDetailRepository viewProductDetailRepository,
            Clock clock
    ) {
        this.viewProductDetailRepository = viewProductDetailRepository;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public ViewProductDetailResult execute(ViewProductDetailCommand command) {
        return viewProductDetailRepository
                .findVisibleByProductId(command.productId(), clock.instant())
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));
    }

    public String successMessage() {
        return "Lay chi tiet san pham thanh cong.";
    }
}
