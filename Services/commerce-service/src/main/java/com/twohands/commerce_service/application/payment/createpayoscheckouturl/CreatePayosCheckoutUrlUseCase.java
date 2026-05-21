package com.twohands.commerce_service.application.payment.createpayoscheckouturl;

import com.twohands.commerce_service.domain.order.OrderStatus;
import com.twohands.commerce_service.domain.payment.CreatePayosCheckoutUrlRepository;
import com.twohands.commerce_service.domain.payment.CreatePayosCheckoutUrlResult;
import com.twohands.commerce_service.domain.payment.PaymentMethod;
import com.twohands.commerce_service.domain.payment.PaymentPayosSnapshot;
import com.twohands.commerce_service.domain.payment.PaymentStatus;
import com.twohands.commerce_service.domain.payment.PayosCheckoutUrlGateway;
import com.twohands.commerce_service.domain.payment.PayosCreateLinkCommand;
import com.twohands.commerce_service.domain.payment.PayosPaymentLinkResult;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.Instant;

@Service
public class CreatePayosCheckoutUrlUseCase {

    private final CreatePayosCheckoutUrlRepository createPayosCheckoutUrlRepository;
    private final PayosCheckoutUrlGateway payosCheckoutUrlGateway;
    private final Clock clock;

    public CreatePayosCheckoutUrlUseCase(
            CreatePayosCheckoutUrlRepository createPayosCheckoutUrlRepository,
            PayosCheckoutUrlGateway payosCheckoutUrlGateway,
            Clock clock
    ) {
        this.createPayosCheckoutUrlRepository = createPayosCheckoutUrlRepository;
        this.payosCheckoutUrlGateway = payosCheckoutUrlGateway;
        this.clock = clock;
    }

    public CreatePayosCheckoutUrlResult execute(CreatePayosCheckoutUrlCommand command) {
        Instant now = clock.instant();
        PaymentPayosSnapshot payment = createPayosCheckoutUrlRepository.findPaymentForBuyer(
                        command.paymentId(),
                        command.buyerId()
                )
                .orElseThrow(() -> new AppException(ErrorCode.PAYMENT_NOT_FOUND));

        validatePayosCheckoutEligible(payment);

        if (hasReusableCheckoutUrl(payment, now)) {
            return new CreatePayosCheckoutUrlResult(
                    payment.paymentId(),
                    payment.orderId(),
                    payment.payosOrderCode(),
                    payment.payosCheckoutUrl(),
                    payment.checkoutUrlExpiredAt(),
                    true
            );
        }

        PayosPaymentLinkResult providerResult = payosCheckoutUrlGateway.createPaymentLink(
                buildProviderCommand(payment, now)
        );

        return createPayosCheckoutUrlRepository.savePayosCheckoutFields(
                payment.paymentId(),
                payment.orderId(),
                providerResult,
                now
        );
    }

    public String successMessage(boolean reusedExistingUrl) {
        if (reusedExistingUrl) {
            return "Lay lai link thanh toan payOS thanh cong.";
        }
        return "Tao link thanh toan payOS thanh cong.";
    }

    private void validatePayosCheckoutEligible(PaymentPayosSnapshot payment) {
        if (payment.paymentMethod() != PaymentMethod.PAYOS) {
            throw new AppException(
                    ErrorCode.INVALID_PAYMENT_METHOD,
                    "Payment method must be PAYOS to create checkout URL"
            );
        }
        if (payment.paymentStatus() != PaymentStatus.PENDING) {
            throw new AppException(
                    ErrorCode.INVALID_PAYMENT_STATE,
                    "Payment is not pending"
            );
        }
        if (payment.orderStatus() != OrderStatus.AWAITING_PAYMENT) {
            throw new AppException(
                    ErrorCode.ORDER_NOT_AWAITING_PAYMENT,
                    "Order must be awaiting payment"
            );
        }
    }

    private boolean hasReusableCheckoutUrl(PaymentPayosSnapshot payment, Instant now) {
        if (!StringUtils.hasText(payment.payosCheckoutUrl())) {
            return false;
        }
        Instant expiredAt = payment.checkoutUrlExpiredAt();
        return expiredAt != null && expiredAt.isAfter(now);
    }

    private PayosCreateLinkCommand buildProviderCommand(PaymentPayosSnapshot payment, Instant now) {
        long amountVnd = toVndAmount(payment.amount());
        long orderCode = generateOrderCode(payment.paymentId(), now);
        Instant linkExpiredAt = payment.paymentExpiredAt() != null ? payment.paymentExpiredAt() : now.plusSeconds(30 * 60L);
        return new PayosCreateLinkCommand(
                payment.paymentId(),
                payment.orderId(),
                orderCode,
                amountVnd,
                buildDescription(payment.orderId()),
                linkExpiredAt
        );
    }

    private long toVndAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new AppException(ErrorCode.INVALID_PAYMENT_AMOUNT, "Payment amount must be greater than 0");
        }
        return amount.setScale(0, RoundingMode.HALF_UP).longValueExact();
    }

    private long generateOrderCode(java.util.UUID paymentId, Instant now) {
        long derived = Math.abs(paymentId.getMostSignificantBits() ^ paymentId.getLeastSignificantBits());
        if (derived == 0L) {
            derived = now.toEpochMilli();
        }
        return derived;
    }

    private String buildDescription(java.util.UUID orderId) {
        String suffix = orderId.toString().replace("-", "");
        if (suffix.length() > 7) {
            suffix = suffix.substring(suffix.length() - 7);
        }
        return "DH" + suffix;
    }
}
