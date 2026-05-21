package com.twohands.commerce_service.infrastructure.scheduler;

import com.twohands.commerce_service.application.order.autocompletedeliveredorder.AutoCompleteDeliveredOrdersResult;
import com.twohands.commerce_service.application.order.autocompletedeliveredorder.AutoCompleteDeliveredOrdersUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class AutoCompleteDeliveredOrderScheduler {

    private static final Logger log = LoggerFactory.getLogger(AutoCompleteDeliveredOrderScheduler.class);

    private final AutoCompleteDeliveredOrdersUseCase autoCompleteDeliveredOrdersUseCase;
    private final boolean enabled;

    public AutoCompleteDeliveredOrderScheduler(
            AutoCompleteDeliveredOrdersUseCase autoCompleteDeliveredOrdersUseCase,
            @Value("${commerce.jobs.auto-complete-delivered-order.enabled:false}") boolean enabled
    ) {
        this.autoCompleteDeliveredOrdersUseCase = autoCompleteDeliveredOrdersUseCase;
        this.enabled = enabled;
    }

    @Scheduled(cron = "${commerce.jobs.auto-complete-delivered-order.cron:0 0 * * * *}")
    public void runAutoCompleteJob() {
        if (!enabled) {
            return;
        }

        long startedAt = System.currentTimeMillis();
        AutoCompleteDeliveredOrdersResult result = autoCompleteDeliveredOrdersUseCase.execute();
        long elapsedMs = System.currentTimeMillis() - startedAt;

        if (result.candidatesFound() > 0 || result.itemsCompleted() > 0 || result.failed() > 0) {
            log.info(
                    "Auto-complete delivered order job completed. candidates={}, itemsCompleted={}, ordersCompleted={}, ordersProcessed={}, failed={}, elapsedMs={}",
                    result.candidatesFound(),
                    result.itemsCompleted(),
                    result.ordersCompleted(),
                    result.ordersProcessed(),
                    result.failed(),
                    elapsedMs
            );
        }
    }
}
