package com.twohands.commerce_service.application.support.exportwebhooklogs;

import com.twohands.commerce_service.application.support.viewwebhooklogs.ViewWebhookLogsForSupportQuery;
import com.twohands.commerce_service.application.support.viewwebhooklogs.ViewWebhookLogsForSupportUseCase;
import com.twohands.commerce_service.domain.support.ViewWebhookLogsForSupportRepository;
import com.twohands.commerce_service.domain.support.WebhookLogSupportEntry;
import com.twohands.commerce_service.domain.support.WebhookLogSupportSearchCriteria;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ExportWebhookLogsForSupportUseCase {

    public static final int MAX_EXPORT_ROWS = 5000;

    private final ViewWebhookLogsForSupportRepository viewWebhookLogsForSupportRepository;

    public ExportWebhookLogsForSupportUseCase(ViewWebhookLogsForSupportRepository viewWebhookLogsForSupportRepository) {
        this.viewWebhookLogsForSupportRepository = viewWebhookLogsForSupportRepository;
    }

    @Transactional(readOnly = true)
    public List<WebhookLogSupportEntry> execute(ViewWebhookLogsForSupportQuery query) {
        WebhookLogSupportSearchCriteria criteria = ViewWebhookLogsForSupportUseCase.buildCriteria(query);
        return viewWebhookLogsForSupportRepository.searchAll(criteria, MAX_EXPORT_ROWS);
    }
}
