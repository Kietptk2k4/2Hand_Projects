package com.twohands.commerce_service.application.payment.viewpaymentsupport;

import com.twohands.commerce_service.domain.payment.PaymentMethod;
import com.twohands.commerce_service.domain.payment.PaymentStatus;
import com.twohands.commerce_service.domain.payment.PaymentSupportDetailSnapshot;
import com.twohands.commerce_service.domain.payment.PaymentSupportReconciliationPolicy;
import com.twohands.commerce_service.domain.payment.ViewPaymentSupportDetailRepository;
import com.twohands.commerce_service.domain.payment.ViewPaymentSupportDetailResult;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Clock;
import java.time.Instant;

@Service
public class ViewPaymentSupportDetailUseCase {

    private final ViewPaymentSupportDetailRepository viewPaymentSupportDetailRepository;
    private final Clock clock;

    public ViewPaymentSupportDetailUseCase(
            ViewPaymentSupportDetailRepository viewPaymentSupportDetailRepository,
            Clock clock
    ) {
        this.viewPaymentSupportDetailRepository = viewPaymentSupportDetailRepository;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public ViewPaymentSupportDetailResult execute(ViewPaymentSupportDetailCommand command) {
        PaymentSupportDetailSnapshot payment = viewPaymentSupportDetailRepository
                .findByPaymentId(command.paymentId())
                .orElseThrow(() -> new AppException(ErrorCode.PAYMENT_NOT_FOUND));

        Instant now = clock.instant();
        boolean checkoutUrlAvailable = isCheckoutUrlAvailable(payment, now);
        String reconciliationStatus = PaymentSupportReconciliationPolicy.resolve(
                payment.paymentMethod(),
                payment.status(),
                payment.webhookEvents()
        );

        return new ViewPaymentSupportDetailResult(
                payment.paymentId(),
                payment.orderId(),
                payment.payerId(),
                payment.paymentMethod(),
                payment.amount(),
                payment.currency(),
                payment.status(),
                payment.paidAt(),
                payment.expiredAt(),
                payment.createdAt(),
                payment.updatedAt(),
                payment.payosOrderCode(),
                payment.payosTransactionId(),
                checkoutUrlAvailable,
                payment.checkoutUrlExpiredAt(),
                payment.orderStatus(),
                payment.orderPaymentStatus(),
                reconciliationStatus,
                payment.statusTimeline(),
                payment.webhookEvents()
        );
    }

    public String successMessage() {
        return "Payment support detail retrieved successfully";
    }

    private boolean isCheckoutUrlAvailable(PaymentSupportDetailSnapshot payment, Instant now) {
        if (payment.paymentMethod() != PaymentMethod.PAYOS) {
            return false;
        }
        if (payment.status() != PaymentStatus.PENDING) {
            return false;
        }
        if (!StringUtils.hasText(payment.payosCheckoutUrl())) {
            return false;
        }
        Instant checkoutUrlExpiredAt = payment.checkoutUrlExpiredAt();
        return checkoutUrlExpiredAt != null && checkoutUrlExpiredAt.isAfter(now);
    }
}
