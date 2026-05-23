package com.twohands.commerce_service.application.support.viewwebhooklogs;

public record ViewWebhookLogsForSupportQuery(
        String provider,
        String referenceId,
        String status,
        String from,
        String to,
        Integer page,
        Integer size
) {
}
