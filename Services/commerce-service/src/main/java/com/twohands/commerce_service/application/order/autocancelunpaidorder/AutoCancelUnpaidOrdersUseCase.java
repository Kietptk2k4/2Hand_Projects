package com.twohands.commerce_service.application.order.autocancelunpaidorder;

import com.twohands.commerce_service.domain.order.ExpiredUnpaidOrderCandidate;
import com.twohands.commerce_service.domain.order.UnpaidOrderCancelOutcome;
import com.twohands.commerce_service.domain.order.UnpaidOrderCancellationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class AutoCancelUnpaidOrdersUseCase {

    private static final Logger log = LoggerFactory.getLogger(AutoCancelUnpaidOrdersUseCase.class);

    private final UnpaidOrderCancellationRepository unpaidOrderCancellationRepository;
    private final int batchSize;
    private final int orderTtlMinutes;

    public AutoCancelUnpaidOrdersUseCase(
            UnpaidOrderCancellationRepository unpaidOrderCancellationRepository,
            @Value("${commerce.jobs.auto-cancel-unpaid-order.batch-size:50}") int batchSize,
            @Value("${commerce.jobs.auto-cancel-unpaid-order.order-ttl-minutes:30}") int orderTtlMinutes
    ) {
        this.unpaidOrderCancellationRepository = unpaidOrderCancellationRepository;
        this.batchSize = batchSize;
        this.orderTtlMinutes = orderTtlMinutes;
    }

    public AutoCancelUnpaidOrdersResult execute() {
        Instant now = Instant.now();
        Instant orderCreatedBefore = now.minus(orderTtlMinutes, ChronoUnit.MINUTES);

        List<ExpiredUnpaidOrderCandidate> candidates = unpaidOrderCancellationRepository.findExpiredCandidates(
                batchSize,
                now,
                orderCreatedBefore
        );

        int cancelled = 0;
        int skipped = 0;
        int failed = 0;

        for (ExpiredUnpaidOrderCandidate candidate : candidates) {
            try {
                UnpaidOrderCancelOutcome outcome = unpaidOrderCancellationRepository.cancelExpiredUnpaidOrder(
                        candidate.orderId(),
                        candidate.paymentId(),
                        now
                );
                if (outcome == UnpaidOrderCancelOutcome.CANCELLED) {
                    cancelled++;
                } else {
                    skipped++;
                    log.debug(
                            "Auto-cancel skipped orderId={} paymentId={} outcome={}",
                            candidate.orderId(),
                            candidate.paymentId(),
                            outcome
                    );
                }
            } catch (Exception ex) {
                failed++;
                log.error(
                        "Auto-cancel failed for orderId={} paymentId={}",
                        candidate.orderId(),
                        candidate.paymentId(),
                        ex
                );
            }
        }

        return new AutoCancelUnpaidOrdersResult(candidates.size(), cancelled, skipped, failed);
    }
}
