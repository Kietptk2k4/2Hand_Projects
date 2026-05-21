package com.twohands.commerce_service.application.order.completeorder;

import com.twohands.commerce_service.domain.order.CompleteOrderOutcome;
import com.twohands.commerce_service.domain.order.CompleteOrderResult;
import com.twohands.commerce_service.domain.order.OrderCompletionRepository;
import com.twohands.commerce_service.domain.order.OrderStatus;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class CompleteOrderUseCase {

    public static final String DEFAULT_REASON = "COMPLETE_ORDER";
    public static final String DEFAULT_CHANGED_BY = "SYSTEM:COMPLETE_ORDER";
    public static final String DEFAULT_COMPLETED_BY_OUTBOX = "SYSTEM";

    private final OrderCompletionRepository orderCompletionRepository;

    public CompleteOrderUseCase(OrderCompletionRepository orderCompletionRepository) {
        this.orderCompletionRepository = orderCompletionRepository;
    }

    @Transactional
    public CompleteOrderResponse execute(CompleteOrderCommand command) {
        Instant now = Instant.now();
        String reason = resolveReason(command.reason());
        String changedBy = resolveChangedBy(command.changedBy());
        String completedByOutbox = resolveCompletedByOutbox(command.completedByOutbox());

        CompleteOrderResult result = orderCompletionRepository.completeIfEligible(
                command.orderId(),
                reason,
                changedBy,
                completedByOutbox,
                now
        );

        return switch (result.outcome()) {
            case NOT_FOUND -> throw new AppException(ErrorCode.ORDER_NOT_FOUND);
            case NOT_ELIGIBLE -> throw new AppException(
                    ErrorCode.ORDER_NOT_COMPLETABLE,
                    "Order cannot be completed: all items must be COMPLETED and payment must be PAID"
            );
            case ALREADY_COMPLETED, COMPLETED -> new CompleteOrderResponse(
                    result.orderId(),
                    OrderStatus.COMPLETED,
                    result.completedAt(),
                    result.outcome() == CompleteOrderOutcome.ALREADY_COMPLETED
            );
        };
    }

    public CompleteOrderResult tryComplete(CompleteOrderCommand command) {
        return orderCompletionRepository.completeIfEligible(
                command.orderId(),
                resolveReason(command.reason()),
                resolveChangedBy(command.changedBy()),
                resolveCompletedByOutbox(command.completedByOutbox()),
                Instant.now()
        );
    }

    public String successMessage(boolean alreadyCompleted) {
        if (alreadyCompleted) {
            return "Don hang da hoan tat truoc do.";
        }
        return "Hoan tat don hang thanh cong.";
    }

    private String resolveReason(String reason) {
        if (reason == null || reason.isBlank()) {
            return DEFAULT_REASON;
        }
        return reason.trim();
    }

    private String resolveChangedBy(String changedBy) {
        if (changedBy == null || changedBy.isBlank()) {
            return DEFAULT_CHANGED_BY;
        }
        return changedBy.trim();
    }

    private String resolveCompletedByOutbox(String completedByOutbox) {
        if (completedByOutbox == null || completedByOutbox.isBlank()) {
            return DEFAULT_COMPLETED_BY_OUTBOX;
        }
        return completedByOutbox.trim();
    }
}
