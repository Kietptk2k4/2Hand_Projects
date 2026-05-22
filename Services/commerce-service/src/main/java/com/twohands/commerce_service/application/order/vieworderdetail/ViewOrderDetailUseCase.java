package com.twohands.commerce_service.application.order.vieworderdetail;

import com.twohands.commerce_service.domain.order.ViewOrderDetailRepository;
import com.twohands.commerce_service.domain.order.ViewOrderDetailResult;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ViewOrderDetailUseCase {

    private final ViewOrderDetailRepository viewOrderDetailRepository;

    public ViewOrderDetailUseCase(ViewOrderDetailRepository viewOrderDetailRepository) {
        this.viewOrderDetailRepository = viewOrderDetailRepository;
    }

    @Transactional(readOnly = true)
    public ViewOrderDetailResult execute(ViewOrderDetailCommand command) {
        return viewOrderDetailRepository
                .findByOrderIdAndBuyerId(command.orderId(), command.buyerId())
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));
    }

    public String successMessage() {
        return "Lay chi tiet don hang thanh cong.";
    }
}
