package com.twohands.commerce_service.infrastructure.scheduler;

import com.twohands.commerce_service.application.cart.synccartitemstatus.SyncCartItemStatusUseCase;
import com.twohands.commerce_service.domain.cart.SyncCartItemStatusResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class SyncCartItemStatusScheduler {

    private static final Logger log = LoggerFactory.getLogger(SyncCartItemStatusScheduler.class);

    private final SyncCartItemStatusUseCase syncCartItemStatusUseCase;
    private final boolean enabled;

    public SyncCartItemStatusScheduler(
            SyncCartItemStatusUseCase syncCartItemStatusUseCase,
            @Value("${commerce.jobs.sync-cart-item-status.enabled:false}") boolean enabled
    ) {
        this.syncCartItemStatusUseCase = syncCartItemStatusUseCase;
        this.enabled = enabled;
    }

    @Scheduled(cron = "${commerce.jobs.sync-cart-item-status.cron:0 */5 * * * *}")
    public void runSyncJob() {
        if (!enabled) {
            return;
        }

        long startedAt = System.currentTimeMillis();
        SyncCartItemStatusResult result = syncCartItemStatusUseCase.syncBatch();
        long elapsedMs = System.currentTimeMillis() - startedAt;

        if (result.candidatesScanned() > 0 || result.updated() > 0) {
            log.info(
                    "Cart item status sync job completed. scanned={}, updated={}, unchanged={}, skipped={}, elapsedMs={}",
                    result.candidatesScanned(),
                    result.updated(),
                    result.unchanged(),
                    result.skipped(),
                    elapsedMs
            );
        }
    }
}
