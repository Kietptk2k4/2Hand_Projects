package com.twohands.commerce_service.application.payment.viewpaymentstatus;

import com.twohands.commerce_service.domain.payment.PaymentMethod;
import com.twohands.commerce_service.domain.payment.PaymentStatus;
import com.twohands.commerce_service.domain.payment.ViewPaymentStatusRepository;
import com.twohands.commerce_service.domain.payment.ViewPaymentStatusResult;
import com.twohands.commerce_service.domain.payment.ViewPaymentStatusSnapshot;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Clock;
import java.time.Instant;

@Service
public class ViewPaymentStatusUseCase {

    private final ViewPaymentStatusRepository viewPaymentStatusRepository;
    private final Clock clock;

    public ViewPaymentStatusUseCase(
            ViewPaymentStatusRepository viewPaymentStatusRepository,
            Clock clock
    ) {
        this.viewPaymentStatusRepository = viewPaymentStatusRepository;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public ViewPaymentStatusResult execute(ViewPaymentStatusCommand command) {
        ViewPaymentStatusSnapshot payment = viewPaymentStatusRepository
                .findByPaymentIdAndBuyerId(command.paymentId(), command.buyerId())
                .orElseThrow(() -> new AppException(ErrorCode.PAYMENT_NOT_FOUND));

        Instant now = clock.instant();
        return new ViewPaymentStatusResult(
                payment.paymentId(),
                payment.orderId(),
                payment.paymentMethod(),
                payment.amount(),
                payment.currency(),
                payment.status(),
                payment.paidAt(),
                payment.expiredAt(),
                resolvePayosCheckoutUrl(payment, now),
                payment.orderStatus(),
                payment.orderPaymentStatus()
        );
    }

    public String successMessage() {
        return "Lay trang thai thanh toan thanh cong.";
    }

    private String resolvePayosCheckoutUrl(ViewPaymentStatusSnapshot payment, Instant now) {
        if (payment.paymentMethod() != PaymentMethod.PAYOS) {
            return null;
        }
        if (payment.status() != PaymentStatus.PENDING) {
            return null;
        }
        if (!StringUtils.hasText(payment.payosCheckoutUrl())) {
            return null;
        }
        Instant checkoutUrlExpiredAt = payment.checkoutUrlExpiredAt();
        if (checkoutUrlExpiredAt == null || !checkoutUrlExpiredAt.isAfter(now)) {
            return null;
        }
        return payment.payosCheckoutUrl();
    }
}
