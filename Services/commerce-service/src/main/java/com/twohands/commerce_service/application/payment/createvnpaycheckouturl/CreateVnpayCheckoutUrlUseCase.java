package com.twohands.commerce_service.application.payment.createvnpaycheckouturl;

import com.twohands.commerce_service.config.CommerceCheckoutProperties;
import com.twohands.commerce_service.domain.order.OrderStatus;
import com.twohands.commerce_service.domain.payment.CreateVnpayCheckoutUrlRepository;
import com.twohands.commerce_service.domain.payment.CreateVnpayCheckoutUrlResult;
import com.twohands.commerce_service.domain.payment.PaymentMethod;
import com.twohands.commerce_service.domain.payment.PaymentStatus;
import com.twohands.commerce_service.domain.payment.PaymentVnpaySnapshot;
import com.twohands.commerce_service.domain.payment.VnpayCheckoutUrlGateway;
import com.twohands.commerce_service.domain.payment.VnpayCreatePaymentUrlCommand;
import com.twohands.commerce_service.domain.payment.VnpayPaymentUrlResult;
import com.twohands.commerce_service.domain.payment.VnpayTxnRefParser;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

@Service
public class CreateVnpayCheckoutUrlUseCase {

    private final CreateVnpayCheckoutUrlRepository createVnpayCheckoutUrlRepository;
    private final VnpayCheckoutUrlGateway vnpayCheckoutUrlGateway;
    private final Clock clock;
    private final CommerceCheckoutProperties checkoutProperties;

    public CreateVnpayCheckoutUrlUseCase(
            CreateVnpayCheckoutUrlRepository createVnpayCheckoutUrlRepository,
            VnpayCheckoutUrlGateway vnpayCheckoutUrlGateway,
            Clock clock,
            CommerceCheckoutProperties checkoutProperties
    ) {
        this.createVnpayCheckoutUrlRepository = createVnpayCheckoutUrlRepository;
        this.vnpayCheckoutUrlGateway = vnpayCheckoutUrlGateway;
        this.clock = clock;
        this.checkoutProperties = checkoutProperties;
    }

    public CreateVnpayCheckoutUrlResult execute(CreateVnpayCheckoutUrlCommand command) {
        PaymentVnpaySnapshot payment = createVnpayCheckoutUrlRepository.findPaymentForBuyer(
                        command.paymentId(),
                        command.buyerId()
                )
                .orElseThrow(() -> new AppException(ErrorCode.PAYMENT_NOT_FOUND));

        return createCheckoutUrl(payment, command.clientIp(), command.frontendReturnUrl(), command.vnpayReturnUrl());
    }

    public CreateVnpayCheckoutUrlResult executeForOrder(
            UUID orderId,
            UUID buyerId,
            String clientIp,
            String frontendReturnUrl,
            String vnpayReturnUrl
    ) {
        PaymentVnpaySnapshot payment = createVnpayCheckoutUrlRepository.findPaymentByOrderForBuyer(orderId, buyerId)
                .orElseThrow(() -> new AppException(ErrorCode.PAYMENT_NOT_FOUND));

        return createCheckoutUrl(payment, clientIp, frontendReturnUrl, vnpayReturnUrl);
    }

    private CreateVnpayCheckoutUrlResult createCheckoutUrl(
            PaymentVnpaySnapshot payment,
            String clientIp,
            String frontendReturnUrl,
            String vnpayReturnUrl
    ) {
        validateVnpayCheckoutEligible(payment);

        Instant now = clock.instant();
        String txnRef = VnpayTxnRefParser.buildTxnRef(payment.orderId(), now);
        VnpayPaymentUrlResult providerResult = vnpayCheckoutUrlGateway.createPaymentUrl(
                new VnpayCreatePaymentUrlCommand(
                        payment.paymentId(),
                        payment.orderId(),
                        payment.amount(),
                        txnRef,
                        buildDescription(payment.orderId()),
                        clientIp,
                        now,
                        vnpayReturnUrl
                )
        );

        return createVnpayCheckoutUrlRepository.saveVnpayCheckoutFields(
                payment.paymentId(),
                payment.orderId(),
                providerResult,
                now,
                frontendReturnUrl
        );
    }

    private void validateVnpayCheckoutEligible(PaymentVnpaySnapshot payment) {
        if (checkoutProperties.isCodOnlyEnabled()) {
            throw new AppException(
                    ErrorCode.INVALID_PAYMENT_METHOD,
                    "VNPay checkout is disabled while cod-only mode is enabled"
            );
        }
        if (payment.paymentMethod() != PaymentMethod.VNPAY) {
            throw new AppException(
                    ErrorCode.INVALID_PAYMENT_METHOD,
                    "Payment method must be VNPAY to create checkout URL"
            );
        }
        if (payment.paymentStatus() != PaymentStatus.PENDING) {
            throw new AppException(ErrorCode.INVALID_PAYMENT_STATE, "Payment is not pending");
        }
        if (payment.orderStatus() != OrderStatus.AWAITING_PAYMENT) {
            throw new AppException(ErrorCode.ORDER_NOT_AWAITING_PAYMENT, "Order must be awaiting payment");
        }
        validateAmount(payment.amount());
    }

    private void validateAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new AppException(ErrorCode.INVALID_PAYMENT_AMOUNT, "Payment amount must be greater than 0");
        }
        amount.setScale(0, RoundingMode.HALF_UP);
    }

    private String buildDescription(UUID orderId) {
        String suffix = orderId.toString().replace("-", "");
        if (suffix.length() > 7) {
            suffix = suffix.substring(suffix.length() - 7);
        }
        return "Thanh toan don hang DH" + suffix;
    }
}
