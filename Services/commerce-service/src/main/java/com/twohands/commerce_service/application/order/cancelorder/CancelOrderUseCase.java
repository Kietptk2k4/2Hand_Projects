package com.twohands.commerce_service.application.order.cancelorder;

import com.twohands.commerce_service.domain.order.BuyerOrderCancelOutcome;
import com.twohands.commerce_service.domain.order.BuyerOrderCancellationResult;
import com.twohands.commerce_service.domain.order.OrderCancellationRepository;
import com.twohands.commerce_service.domain.order.OrderStatus;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class CancelOrderUseCase {

    private final OrderCancellationRepository orderCancellationRepository;

    public CancelOrderUseCase(OrderCancellationRepository orderCancellationRepository) {
        this.orderCancellationRepository = orderCancellationRepository;
    }

    @Transactional
    public CancelOrderResult execute(CancelOrderCommand command) {
        Instant now = Instant.now();
        BuyerOrderCancellationResult result = orderCancellationRepository.cancelByBuyer(
                command.orderId(),
                command.buyerId(),
                command.reason(),
                now
        );

        return mapResult(command.orderId(), result);
    }

    public static CancelOrderResult mapResult(java.util.UUID orderId, BuyerOrderCancellationResult result) {
        return switch (result.outcome()) {
            case NOT_FOUND -> throw new AppException(ErrorCode.ORDER_NOT_FOUND);
            case NOT_CANCELLABLE -> throw new AppException(
                    ErrorCode.ORDER_NOT_CANCELLABLE,
                    "Order cannot be cancelled in its current state"
            );
            case ALREADY_CANCELLED -> new CancelOrderResult(
                    orderId,
                    OrderStatus.CANCELLED,
                    result.cancelledAt(),
                    true
            );
            case CANCELLED -> new CancelOrderResult(
                    orderId,
                    OrderStatus.CANCELLED,
                    result.cancelledAt(),
                    false
            );
            case PENDING_REFUND, REFUND_ALREADY_REQUESTED -> new CancelOrderResult(
                    orderId,
                    OrderStatus.PROCESSING,
                    result.cancelledAt(),
                    false,
                    true,
                    result.refundRequestId()
            );
        };
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
