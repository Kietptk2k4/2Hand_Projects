package com.twohands.commerce_service.application.payment.createpayment;

import com.twohands.commerce_service.domain.payment.CreatePaymentRepository;
import com.twohands.commerce_service.domain.payment.CreatePaymentRequest;
import com.twohands.commerce_service.domain.payment.CreatePaymentResult;
import com.twohands.commerce_service.domain.payment.PaymentMethod;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class CreatePaymentUseCase {

    private final CreatePaymentRepository createPaymentRepository;

    public CreatePaymentUseCase(CreatePaymentRepository createPaymentRepository) {
        this.createPaymentRepository = createPaymentRepository;
    }

    @Transactional
    public CreatePaymentResult execute(CreatePaymentRequest request) {
        if (request.amount() == null || request.amount().compareTo(BigDecimal.ZERO) < 0) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "Payment amount must be >= 0", "amount", ">= 0");
        }
        if (request.paymentMethod() != PaymentMethod.COD && request.paymentMethod() != PaymentMethod.PAYOS) {
            throw new AppException(ErrorCode.INVALID_PAYMENT_METHOD);
        }
        return createPaymentRepository.createPayment(request);
    }
}
