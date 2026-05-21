package com.twohands.commerce_service.application.checkout.checkoutfromcart;

import com.twohands.commerce_service.domain.checkout.CheckoutFromCartRepository;
import com.twohands.commerce_service.domain.checkout.CheckoutFromCartRequest;
import com.twohands.commerce_service.domain.checkout.CheckoutFromCartResult;
import com.twohands.commerce_service.domain.payment.PaymentMethod;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CheckoutFromCartUseCase {

    private final CheckoutFromCartRepository checkoutFromCartRepository;

    public CheckoutFromCartUseCase(CheckoutFromCartRepository checkoutFromCartRepository) {
        this.checkoutFromCartRepository = checkoutFromCartRepository;
    }

    @Transactional
    public CheckoutFromCartResult execute(CheckoutFromCartCommand command) {
        validatePaymentMethod(command.paymentMethod());

        return checkoutFromCartRepository.checkout(new CheckoutFromCartRequest(
                command.buyerId(),
                command.cartItemIds(),
                command.addressId(),
                command.paymentMethod(),
                command.shipmentType(),
                command.idempotencyKey()
        ));
    }

    public String successMessage(boolean idempotentReplay) {
        if (idempotentReplay) {
            return "Don hang da duoc tao truoc do (idempotency).";
        }
        return "Checkout thanh cong.";
    }

    private void validatePaymentMethod(PaymentMethod paymentMethod) {
        if (paymentMethod == null) {
            throw new AppException(
                    ErrorCode.VALIDATION_ERROR,
                    "Payment method is required",
                    "payment_method",
                    "must not be null"
            );
        }
        if (paymentMethod != PaymentMethod.COD && paymentMethod != PaymentMethod.PAYOS) {
            throw new AppException(ErrorCode.INVALID_PAYMENT_METHOD);
        }
    }
}
