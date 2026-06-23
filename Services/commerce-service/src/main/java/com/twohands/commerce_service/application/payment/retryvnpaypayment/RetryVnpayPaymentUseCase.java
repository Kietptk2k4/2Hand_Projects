package com.twohands.commerce_service.application.payment.retryvnpaypayment;

import com.twohands.commerce_service.application.payment.createvnpaycheckouturl.CreateVnpayCheckoutUrlUseCase;
import com.twohands.commerce_service.domain.payment.CreateVnpayCheckoutUrlResult;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class RetryVnpayPaymentUseCase {

    private final CreateVnpayCheckoutUrlUseCase createVnpayCheckoutUrlUseCase;

    public RetryVnpayPaymentUseCase(CreateVnpayCheckoutUrlUseCase createVnpayCheckoutUrlUseCase) {
        this.createVnpayCheckoutUrlUseCase = createVnpayCheckoutUrlUseCase;
    }

    public CreateVnpayCheckoutUrlResult execute(RetryVnpayPaymentCommand command) {
        return createVnpayCheckoutUrlUseCase.executeForOrder(
                command.orderId(),
                command.buyerId(),
                command.clientIp(),
                command.frontendReturnUrl(),
                command.vnpayReturnUrl()
        );
    }
}
