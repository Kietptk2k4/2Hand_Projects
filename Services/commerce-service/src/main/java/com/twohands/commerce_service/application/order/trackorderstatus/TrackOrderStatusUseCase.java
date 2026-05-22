package com.twohands.commerce_service.application.order.trackorderstatus;

import com.twohands.commerce_service.domain.order.TrackOrderStatusRepository;
import com.twohands.commerce_service.domain.order.TrackOrderStatusResult;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TrackOrderStatusUseCase {

    private final TrackOrderStatusRepository trackOrderStatusRepository;

    public TrackOrderStatusUseCase(TrackOrderStatusRepository trackOrderStatusRepository) {
        this.trackOrderStatusRepository = trackOrderStatusRepository;
    }

    @Transactional(readOnly = true)
    public TrackOrderStatusResult execute(TrackOrderStatusCommand command) {
        return trackOrderStatusRepository
                .findByOrderIdAndBuyerId(command.orderId(), command.buyerId())
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));
    }

    public String successMessage() {
        return "Lay trang thai don hang thanh cong.";
    }
}
