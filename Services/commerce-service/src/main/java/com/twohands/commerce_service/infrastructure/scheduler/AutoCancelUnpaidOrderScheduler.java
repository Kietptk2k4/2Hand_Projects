package com.twohands.commerce_service.infrastructure.scheduler;

import com.twohands.commerce_service.application.order.autocancelunpaidorder.AutoCancelUnpaidOrdersResult;
import com.twohands.commerce_service.application.order.autocancelunpaidorder.AutoCancelUnpaidOrdersUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class AutoCancelUnpaidOrderScheduler {

    private static final Logger log = LoggerFactory.getLogger(AutoCancelUnpaidOrderScheduler.class);

    private final AutoCancelUnpaidOrdersUseCase autoCancelUnpaidOrdersUseCase;
    private final boolean enabled;

    public AutoCancelUnpaidOrderScheduler(
            AutoCancelUnpaidOrdersUseCase autoCancelUnpaidOrdersUseCase,
            @Value("${commerce.jobs.auto-cancel-unpaid-order.enabled:false}") boolean enabled
    ) {
        this.autoCancelUnpaidOrdersUseCase = autoCancelUnpaidOrdersUseCase;
        this.enabled = enabled;
    }

    @Scheduled(cron = "${commerce.jobs.auto-cancel-unpaid-order.cron:0 */10 * * * *}")
    public void runAutoCancelJob() {
        if (!enabled) {
            return;
        }

        long startedAt = System.currentTimeMillis();
        AutoCancelUnpaidOrdersResult result = autoCancelUnpaidOrdersUseCase.execute();
        long elapsedMs = System.currentTimeMillis() - startedAt;

        if (result.candidatesFound() > 0 || result.cancelled() > 0 || result.failed() > 0) {
            log.info(
                    "Auto-cancel unpaid order job completed. candidates={}, cancelled={}, skipped={}, failed={}, elapsedMs={}",
                    result.candidatesFound(),
                    result.cancelled(),
                    result.skipped(),
                    result.failed(),
                    elapsedMs
            );
        }
    }
}
