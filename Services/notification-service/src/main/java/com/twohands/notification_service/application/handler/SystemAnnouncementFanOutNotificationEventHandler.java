package com.twohands.notification_service.application.handler;

import com.twohands.notification_service.application.announcement.FanOutSystemAnnouncementCommand;
import com.twohands.notification_service.application.announcement.FanOutSystemAnnouncementResult;
import com.twohands.notification_service.application.announcement.FanOutSystemAnnouncementUseCase;
import com.twohands.notification_service.application.announcement.ResolveSystemAnnouncementRecipientsUseCase;
import com.twohands.notification_service.application.worker.NotificationFailurePolicy;
import com.twohands.notification_service.domain.admin.SystemAnnouncementFanOutContext;
import com.twohands.notification_service.domain.announcement.SystemAnnouncementAudienceUserProvider;
import com.twohands.notification_service.domain.notificationevent.NotificationEvent;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@Order(30)
public class SystemAnnouncementFanOutNotificationEventHandler implements NotificationEventHandler {

    private static final String SYSTEM_ANNOUNCEMENT_SENT = "SYSTEM_ANNOUNCEMENT_SENT";

    private final SystemAnnouncementFanOutPayloadParser payloadParser;
    private final ResolveSystemAnnouncementRecipientsUseCase resolveSystemAnnouncementRecipientsUseCase;
    private final FanOutSystemAnnouncementUseCase fanOutSystemAnnouncementUseCase;
    private final SystemAnnouncementAudienceUserProvider audienceUserProvider;

    public SystemAnnouncementFanOutNotificationEventHandler(
            SystemAnnouncementFanOutPayloadParser payloadParser,
            ResolveSystemAnnouncementRecipientsUseCase resolveSystemAnnouncementRecipientsUseCase,
            FanOutSystemAnnouncementUseCase fanOutSystemAnnouncementUseCase,
            SystemAnnouncementAudienceUserProvider audienceUserProvider
    ) {
        this.payloadParser = payloadParser;
        this.resolveSystemAnnouncementRecipientsUseCase = resolveSystemAnnouncementRecipientsUseCase;
        this.fanOutSystemAnnouncementUseCase = fanOutSystemAnnouncementUseCase;
        this.audienceUserProvider = audienceUserProvider;
    }

    @Override
    public boolean supports(String eventType) {
        return SYSTEM_ANNOUNCEMENT_SENT.equals(eventType);
    }

    @Override
    public NotificationEventHandlerResult handle(NotificationEvent event) {
        SystemAnnouncementFanOutContext context;
        try {
            context = payloadParser.parse(event);
        } catch (IllegalArgumentException ex) {
            return NotificationEventHandlerResult.failure(ex.getMessage(), NotificationFailurePolicy.PERMANENT);
        }

        List<UUID> recipients;
        try {
            recipients = resolveSystemAnnouncementRecipientsUseCase.execute(context);
        } catch (IllegalArgumentException ex) {
            NotificationFailurePolicy policy = isAudienceResolverUnavailable(context, ex)
                    ? NotificationFailurePolicy.RETRYABLE
                    : NotificationFailurePolicy.PERMANENT;
            return NotificationEventHandlerResult.failure(ex.getMessage(), policy);
        }

        FanOutSystemAnnouncementResult result = fanOutSystemAnnouncementUseCase.execute(
                new FanOutSystemAnnouncementCommand(event.id(), context, recipients)
        );
        if (result.hasFailure()) {
            return result.failure();
        }
        if (!result.delivered()) {
            return NotificationEventHandlerResult.noOp();
        }
        return NotificationEventHandlerResult.success();
    }

    private boolean isAudienceResolverUnavailable(
            SystemAnnouncementFanOutContext context,
            IllegalArgumentException ex
    ) {
        if (context.explicitRecipientUserIds() != null && !context.explicitRecipientUserIds().isEmpty()) {
            return false;
        }
        String message = ex.getMessage();
        if (message == null) {
            return false;
        }
        return message.contains("audience provider is not configured")
                && context.targetAudience() != null
                && audienceUserProvider.supports(context.targetAudience());
    }
}
