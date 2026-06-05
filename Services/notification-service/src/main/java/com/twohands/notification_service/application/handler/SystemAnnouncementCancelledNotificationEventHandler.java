package com.twohands.notification_service.application.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.twohands.notification_service.application.announcement.WithdrawSystemAnnouncementNotificationsUseCase;
import com.twohands.notification_service.application.worker.NotificationFailurePolicy;
import com.twohands.notification_service.domain.notificationevent.NotificationEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;

@Component
@Order(31)
public class SystemAnnouncementCancelledNotificationEventHandler implements NotificationEventHandler {

    private static final Logger log = LoggerFactory.getLogger(SystemAnnouncementCancelledNotificationEventHandler.class);
    private static final String SYSTEM_ANNOUNCEMENT_CANCELLED = "SYSTEM_ANNOUNCEMENT_CANCELLED";

    private final ObjectMapper objectMapper;
    private final WithdrawSystemAnnouncementNotificationsUseCase withdrawSystemAnnouncementNotificationsUseCase;

    public SystemAnnouncementCancelledNotificationEventHandler(
            ObjectMapper objectMapper,
            WithdrawSystemAnnouncementNotificationsUseCase withdrawSystemAnnouncementNotificationsUseCase
    ) {
        this.objectMapper = objectMapper;
        this.withdrawSystemAnnouncementNotificationsUseCase = withdrawSystemAnnouncementNotificationsUseCase;
    }

    @Override
    public boolean supports(String eventType) {
        return SYSTEM_ANNOUNCEMENT_CANCELLED.equals(eventType);
    }

    @Override
    public NotificationEventHandlerResult handle(NotificationEvent event) {
        String announcementId;
        try {
            announcementId = resolveAnnouncementId(event);
        } catch (IllegalArgumentException ex) {
            return NotificationEventHandlerResult.failure(ex.getMessage(), NotificationFailurePolicy.PERMANENT);
        }

        try {
            int withdrawn = withdrawSystemAnnouncementNotificationsUseCase.execute(announcementId);
            log.info(
                    "Processed system announcement cancellation. eventId={}, announcementId={}, withdrawnCount={}",
                    event.id(),
                    announcementId,
                    withdrawn
            );
            return NotificationEventHandlerResult.success();
        } catch (DataAccessException ex) {
            return NotificationEventHandlerResult.failure(
                    "Failed to withdraw system announcement notifications",
                    NotificationFailurePolicy.RETRYABLE
            );
        }
    }

    private String resolveAnnouncementId(NotificationEvent event) {
        if ("SYSTEM_ANNOUNCEMENT".equalsIgnoreCase(event.aggregateType())
                && event.aggregateId() != null
                && !event.aggregateId().isBlank()) {
            return event.aggregateId().trim();
        }

        JsonNode payload = parsePayload(event.payload());
        String announcementId = textField(payload, "announcement_id");
        if (announcementId == null) {
            throw new IllegalArgumentException("announcement_id is required for SYSTEM_ANNOUNCEMENT_CANCELLED event.");
        }
        return announcementId;
    }

    private JsonNode parsePayload(String rawPayload) {
        if (rawPayload == null || rawPayload.isBlank()) {
            return objectMapper.createObjectNode();
        }
        try {
            JsonNode node = objectMapper.readTree(rawPayload);
            return node == null || node.isNull() ? objectMapper.createObjectNode() : node;
        } catch (Exception ex) {
            throw new IllegalArgumentException("SYSTEM_ANNOUNCEMENT_CANCELLED event payload must be valid JSON.");
        }
    }

    private String textField(JsonNode payload, String field) {
        JsonNode node = payload.get(field);
        if (node == null || node.isNull() || !node.isValueNode()) {
            return null;
        }
        String value = node.asText();
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
