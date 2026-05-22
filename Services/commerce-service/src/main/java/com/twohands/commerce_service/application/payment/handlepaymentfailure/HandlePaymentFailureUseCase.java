package com.twohands.commerce_service.application.payment.handlepaymentfailure;

import com.twohands.commerce_service.domain.payment.HandlePaymentFailureRepository;
import com.twohands.commerce_service.domain.payment.HandlePaymentFailureResult;
import com.twohands.commerce_service.domain.payment.LockedPaymentContext;
import com.twohands.commerce_service.domain.payment.PaymentFailureOutcome;
import com.twohands.commerce_service.domain.payment.PaymentStatus;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

@Service
public class HandlePaymentFailureUseCase {

    private static final Logger log = LoggerFactory.getLogger(HandlePaymentFailureUseCase.class);

    private final HandlePaymentFailureRepository handlePaymentFailureRepository;
    private final Clock clock;

    public HandlePaymentFailureUseCase(
            HandlePaymentFailureRepository handlePaymentFailureRepository,
            Clock clock
    ) {
        this.handlePaymentFailureRepository = handlePaymentFailureRepository;
        this.clock = clock;
    }

    @Transactional
    public HandlePaymentFailureResult execute(HandlePaymentFailureCommand command) {
        validateCommand(command);

        LockedPaymentContext payment = resolvePayment(command)
                .orElseThrow(() -> new AppException(ErrorCode.PAYMENT_NOT_FOUND));

        if (payment.isPaid()) {
            log.warn(
                    "Ignoring payment failure for paymentId={} because status is already PAID (reason={})",
                    payment.paymentId(),
                    command.reason()
            );
            return skipped(payment, command.terminalStatus(), PaymentFailureOutcome.SKIPPED_ALREADY_PAID);
        }

        Instant now = clock.instant();
        return handlePaymentFailureRepository.handleFailure(
                payment,
                command.terminalStatus(),
                command.reason(),
                command.changedBy(),
                command.historyPayloadJson(),
                now
        );
    }

    private java.util.Optional<LockedPaymentContext> resolvePayment(HandlePaymentFailureCommand command) {
        if (command.paymentId() != null) {
            return handlePaymentFailureRepository.lockPaymentById(command.paymentId());
        }
        if (StringUtils.hasText(command.payosOrderCode())) {
            return handlePaymentFailureRepository.lockPaymentByPayosOrderCode(command.payosOrderCode().trim());
        }
        return java.util.Optional.empty();
    }

    private HandlePaymentFailureResult skipped(
            LockedPaymentContext payment,
            PaymentStatus terminalStatus,
            PaymentFailureOutcome outcome
    ) {
        return new HandlePaymentFailureResult(
                outcome,
                payment.paymentId(),
                payment.orderId(),
                terminalStatus,
                false,
                null
        );
    }

    private void validateCommand(HandlePaymentFailureCommand command) {
        if (command.paymentId() == null && !StringUtils.hasText(command.payosOrderCode())) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "paymentId or payosOrderCode is required");
        }
        if (!StringUtils.hasText(command.reason())) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "reason is required");
        }
        if (!StringUtils.hasText(command.changedBy())) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "changedBy is required");
        }
    }
}
