package com.twohands.notification_service.delivery.http.internal;

import com.twohands.notification_service.application.ingest.IngestNotificationEventResult;
import com.twohands.notification_service.application.ingest.IngestNotificationEventUseCase;
import com.twohands.notification_service.application.ingest.NotificationEventIngestCommand;
import com.twohands.notification_service.common.dto.ApiResponse;
import com.twohands.notification_service.delivery.http.internal.request.IngestNotificationEventRequest;
import com.twohands.notification_service.delivery.http.internal.response.IngestNotificationEventResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/notification/internal")
public class InternalEventController {

    private final IngestNotificationEventUseCase ingestNotificationEventUseCase;

    public InternalEventController(IngestNotificationEventUseCase ingestNotificationEventUseCase) {
        this.ingestNotificationEventUseCase = ingestNotificationEventUseCase;
    }

    @PostMapping("/events")
    public ResponseEntity<ApiResponse<IngestNotificationEventResponse>> ingest(
            @Valid @RequestBody IngestNotificationEventRequest request
    ) {
        IngestNotificationEventResult result = ingestNotificationEventUseCase.execute(
                new NotificationEventIngestCommand(
                        request.sourceEventId(),
                        request.eventKey(),
                        request.eventType(),
                        request.sourceService(),
                        request.aggregateType(),
                        request.aggregateId(),
                        request.actorId(),
                        request.recipientUserId(),
                        request.payload()
                )
        );

        IngestNotificationEventResponse response = new IngestNotificationEventResponse(
                result.notificationEventId(),
                result.duplicate()
        );

        int status = result.duplicate() ? HttpStatus.OK.value() : HttpStatus.CREATED.value();
        String message = result.duplicate() ? "Event already ingested" : "Event ingested";
        return ResponseEntity.status(status).body(ApiResponse.success(status, message, response));
    }
}
