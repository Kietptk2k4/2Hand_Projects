package com.twohands.commerce_service.domain.support;

public interface ViewWebhookLogsForSupportRepository {

    WebhookLogSupportPagedResult search(WebhookLogSupportSearchCriteria criteria, WebhookSupportPageRequest pageRequest);
}
