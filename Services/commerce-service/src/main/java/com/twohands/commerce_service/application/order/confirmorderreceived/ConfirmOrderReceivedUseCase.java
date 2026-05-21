package com.twohands.commerce_service.application.order.confirmorderreceived;

import com.twohands.commerce_service.domain.order.ConfirmOrderReceivedRepository;
import com.twohands.commerce_service.domain.order.ConfirmOrderReceivedResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class ConfirmOrderReceivedUseCase {

    private final ConfirmOrderReceivedRepository confirmOrderReceivedRepository;

    public ConfirmOrderReceivedUseCase(ConfirmOrderReceivedRepository confirmOrderReceivedRepository) {
        this.confirmOrderReceivedRepository = confirmOrderReceivedRepository;
    }

    @Transactional
    public ConfirmOrderReceivedResult execute(ConfirmOrderReceivedCommand command) {
        return confirmOrderReceivedRepository.confirmReceivedByBuyer(
                command.buyerId(),
                command.orderId(),
                Instant.now()
        );
    }

    public String successMessage(boolean alreadyCompleted) {
        if (alreadyCompleted) {
            return "Don hang da duoc xac nhan nhan hang truoc do.";
        }
        return "Xac nhan da nhan hang thanh cong.";
    }
}
