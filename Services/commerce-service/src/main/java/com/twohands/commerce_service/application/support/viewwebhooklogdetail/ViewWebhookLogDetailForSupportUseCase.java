package com.twohands.commerce_service.application.support.viewwebhooklogdetail;

import com.twohands.commerce_service.domain.support.ViewWebhookLogsForSupportRepository;
import com.twohands.commerce_service.domain.support.WebhookLogSupportEntry;
import com.twohands.commerce_service.domain.support.WebhookLogSupportQueryPolicy;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class ViewWebhookLogDetailForSupportUseCase {

    private final ViewWebhookLogsForSupportRepository viewWebhookLogsForSupportRepository;

    public ViewWebhookLogDetailForSupportUseCase(ViewWebhookLogsForSupportRepository viewWebhookLogsForSupportRepository) {
        this.viewWebhookLogsForSupportRepository = viewWebhookLogsForSupportRepository;
    }

    @Transactional(readOnly = true)
    public WebhookLogSupportEntry execute(UUID logId, String provider) {
        String normalizedProvider = WebhookLogSupportQueryPolicy.normalizeProvider(provider);
        if (normalizedProvider == null) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, ErrorCode.VALIDATION_ERROR.defaultMessage(), "provider", "provider is required");
        }
        return viewWebhookLogsForSupportRepository.findById(logId, normalizedProvider)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Webhook log not found"));
    }

    public String successMessage() {
        return "Webhook log retrieved successfully";
    }
}
