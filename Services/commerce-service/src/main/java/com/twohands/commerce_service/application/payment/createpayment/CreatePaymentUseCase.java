package com.twohands.commerce_service.application.payment.createpayment;

import com.twohands.commerce_service.application.payment.common.PaymentCreatedOutboxService;
import com.twohands.commerce_service.domain.outbox.OutboxEventRepository;
import com.twohands.commerce_service.domain.payment.CreatePaymentRepository;
import com.twohands.commerce_service.domain.payment.CreatePaymentRequest;
import com.twohands.commerce_service.domain.payment.CreatePaymentResult;
import com.twohands.commerce_service.domain.payment.PaymentMethod;
import com.twohands.commerce_service.domain.payment.PaymentStatus;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

@Service
public class CreatePaymentUseCase {

    private static final String DEFAULT_CURRENCY = "VND";

    private final CreatePaymentRepository createPaymentRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final PaymentCreatedOutboxService paymentCreatedOutboxService;

    public CreatePaymentUseCase(
            CreatePaymentRepository createPaymentRepository,
            OutboxEventRepository outboxEventRepository,
            PaymentCreatedOutboxService paymentCreatedOutboxService
    ) {
        this.createPaymentRepository = createPaymentRepository;
        this.outboxEventRepository = outboxEventRepository;
        this.paymentCreatedOutboxService = paymentCreatedOutboxService;
    }

    @Transactional
    public CreatePaymentResult execute(CreatePaymentCommand command) {
        validate(command);

        if (createPaymentRepository.existsByOrderId(command.orderId())) {
            throw new AppException(ErrorCode.PAYMENT_ALREADY_EXISTS);
        }

        String currency = resolveCurrency(command.currency());
        CreatePaymentRequest request = new CreatePaymentRequest(
                command.paymentId(),
                command.orderId(),
                command.payerId(),
                command.amount(),
                currency,
                command.paymentMethod(),
                command.idempotencyKey(),
                command.occurredAt()
        );

        CreatePaymentResult result = createPaymentRepository.createPayment(request);

        outboxEventRepository.save(paymentCreatedOutboxService.build(
                result.paymentId(),
                result.orderId(),
                command.payerId(),
                result.amount(),
                result.currency(),
                result.paymentMethod(),
                result.status(),
                command.occurredAt()
        ));

        return result;
    }

    private void validate(CreatePaymentCommand command) {
        if (command.paymentId() == null || command.orderId() == null || command.payerId() == null) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "Payment identifiers are required");
        }
        if (command.buyerId() == null || !Objects.equals(command.payerId(), command.buyerId())) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "payer_id must match order buyer", "payer_id", "must match buyer");
        }
        if (command.amount() == null || command.amount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new AppException(ErrorCode.INVALID_PAYMENT_AMOUNT, "Payment amount must be greater than 0", "amount", "> 0");
        }
        if (command.orderFinalAmount() == null || command.amount().compareTo(command.orderFinalAmount()) != 0) {
            throw new AppException(
                    ErrorCode.INVALID_PAYMENT_AMOUNT,
                    "Payment amount must equal order final_amount",
                    "amount",
                    "must equal orders.final_amount"
            );
        }
        if (command.paymentMethod() != PaymentMethod.COD && command.paymentMethod() != PaymentMethod.PAYOS) {
            throw new AppException(ErrorCode.INVALID_PAYMENT_METHOD);
        }
        if (command.orderPaymentMethod() != null && command.paymentMethod() != command.orderPaymentMethod()) {
            throw new AppException(
                    ErrorCode.INVALID_PAYMENT_METHOD,
                    "Payment method must match order payment_method",
                    "payment_method",
                    "must match order"
            );
        }
    }

    private String resolveCurrency(String currency) {
        if (!StringUtils.hasText(currency)) {
            return DEFAULT_CURRENCY;
        }
        return currency.trim().toUpperCase();
    }
}
