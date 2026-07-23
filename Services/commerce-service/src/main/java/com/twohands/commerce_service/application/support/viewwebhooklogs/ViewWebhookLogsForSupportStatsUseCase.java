package com.twohands.commerce_service.application.support.viewwebhooklogs;

import com.twohands.commerce_service.domain.support.ViewWebhookLogsForSupportRepository;
import com.twohands.commerce_service.domain.support.WebhookLogSupportSearchCriteria;
import com.twohands.commerce_service.domain.support.WebhookLogSupportStats;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ViewWebhookLogsForSupportStatsUseCase {

    private final ViewWebhookLogsForSupportRepository viewWebhookLogsForSupportRepository;

    public ViewWebhookLogsForSupportStatsUseCase(ViewWebhookLogsForSupportRepository viewWebhookLogsForSupportRepository) {
        this.viewWebhookLogsForSupportRepository = viewWebhookLogsForSupportRepository;
    }

    @Transactional(readOnly = true)
    public ViewWebhookLogsForSupportStatsResult execute(ViewWebhookLogsForSupportStatsQuery query) {
        WebhookLogSupportSearchCriteria criteria = ViewWebhookLogsForSupportUseCase.buildCriteria(
                new ViewWebhookLogsForSupportQuery(
                        query.provider(),
                        query.referenceId(),
                        query.searchQuery(),
                        query.eventType(),
                        query.status(),
                        query.from(),
                        query.to(),
                        null,
                        null
                )
        );
        WebhookLogSupportStats stats = viewWebhookLogsForSupportRepository.aggregateStats(criteria);
        return ViewWebhookLogsForSupportStatsResult.from(stats);
    }

    public String successMessage() {
        return "Webhook log stats retrieved successfully";
    }
}
