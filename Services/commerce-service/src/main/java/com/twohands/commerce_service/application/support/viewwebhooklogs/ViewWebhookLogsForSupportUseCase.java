package com.twohands.commerce_service.application.support.viewwebhooklogs;

import com.twohands.commerce_service.domain.support.ViewWebhookLogsForSupportRepository;
import com.twohands.commerce_service.domain.support.WebhookLogSupportPagedResult;
import com.twohands.commerce_service.domain.support.WebhookLogSupportQueryPolicy;
import com.twohands.commerce_service.domain.support.WebhookLogSupportSearchCriteria;
import com.twohands.commerce_service.domain.support.WebhookSupportPageRequest;
import com.twohands.commerce_service.domain.support.WebhookSupportPaginationPolicy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class ViewWebhookLogsForSupportUseCase {

    private final ViewWebhookLogsForSupportRepository viewWebhookLogsForSupportRepository;

    public ViewWebhookLogsForSupportUseCase(ViewWebhookLogsForSupportRepository viewWebhookLogsForSupportRepository) {
        this.viewWebhookLogsForSupportRepository = viewWebhookLogsForSupportRepository;
    }

    @Transactional(readOnly = true)
    public ViewWebhookLogsForSupportResult execute(ViewWebhookLogsForSupportQuery query) {
        WebhookLogSupportSearchCriteria criteria = buildCriteria(query);
        WebhookSupportPageRequest pageRequest = WebhookSupportPaginationPolicy.normalize(query.page(), query.size());
        WebhookLogSupportPagedResult page = viewWebhookLogsForSupportRepository.search(criteria, pageRequest);

        return new ViewWebhookLogsForSupportResult(
                page.page(),
                page.size(),
                page.totalElements(),
                page.totalPages(),
                page.items()
        );
    }

    public static WebhookLogSupportSearchCriteria buildCriteria(ViewWebhookLogsForSupportQuery query) {
        Instant from = WebhookLogSupportQueryPolicy.parseInstant(query.from(), "from");
        Instant to = WebhookLogSupportQueryPolicy.parseInstant(query.to(), "to");
        WebhookLogSupportQueryPolicy.validateDateRange(from, to);

        return new WebhookLogSupportSearchCriteria(
                WebhookLogSupportQueryPolicy.normalizeProvider(query.provider()),
                WebhookLogSupportQueryPolicy.normalizeReferenceId(query.referenceId()),
                WebhookLogSupportQueryPolicy.normalizeSearchQuery(query.searchQuery()),
                WebhookLogSupportQueryPolicy.normalizeEventType(query.eventType()),
                WebhookLogSupportQueryPolicy.normalizeProcessingStatus(query.status()),
                from,
                to
        );
    }

    public String successMessage() {
        return "Webhook logs retrieved successfully";
    }
}
