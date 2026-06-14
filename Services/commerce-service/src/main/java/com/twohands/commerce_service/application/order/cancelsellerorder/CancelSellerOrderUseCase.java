package com.twohands.commerce_service.application.order.cancelsellerorder;

import com.twohands.commerce_service.application.order.cancelorder.CancelOrderResult;
import com.twohands.commerce_service.application.order.cancelorder.CancelOrderUseCase;
import com.twohands.commerce_service.domain.order.BuyerOrderCancellationResult;
import com.twohands.commerce_service.domain.order.OrderCancellationRepository;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class CancelSellerOrderUseCase {

    private final OrderCancellationRepository orderCancellationRepository;

    public CancelSellerOrderUseCase(OrderCancellationRepository orderCancellationRepository) {
        this.orderCancellationRepository = orderCancellationRepository;
    }

    @Transactional
    public CancelOrderResult execute(CancelSellerOrderCommand command) {
        BuyerOrderCancellationResult result = orderCancellationRepository.cancelBySeller(
                command.orderId(),
                command.sellerId(),
                command.reason(),
                Instant.now()
        );

        if (result.outcome() == com.twohands.commerce_service.domain.order.BuyerOrderCancelOutcome.NOT_FOUND) {
            throw new AppException(ErrorCode.ORDER_NOT_FOUND);
        }

        return CancelOrderUseCase.mapResult(command.orderId(), result);
    }

    public String successMessage(CancelOrderResult result) {
        if (result.pendingRefund()) {
            return "Yeu cau huy don da duoc ghi nhan. Don hang dang cho hoan tien.";
        }
        if (result.alreadyCancelled()) {
            return "Don hang da duoc huy truoc do.";
        }
        return "Huy don hang thanh cong.";
    }
}
