package com.twohands.commerce_service.domain.support;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ViewWebhookLogsForSupportRepository {

    WebhookLogSupportPagedResult search(WebhookLogSupportSearchCriteria criteria, WebhookSupportPageRequest pageRequest);

    Optional<WebhookLogSupportEntry> findById(UUID logId, String provider);

    WebhookLogSupportStats aggregateStats(WebhookLogSupportSearchCriteria criteria);

    List<WebhookLogSupportEntry> searchAll(WebhookLogSupportSearchCriteria criteria, int maxRows);
}
