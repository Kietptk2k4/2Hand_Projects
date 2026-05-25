package com.twohands.notification_service.application.handler;

import com.twohands.notification_service.application.delivery.ApplyNotificationDeliveryRulesCommand;
import com.twohands.notification_service.application.delivery.ApplyNotificationDeliveryRulesUseCase;
import com.twohands.notification_service.application.delivery.ApplySkipSelfNotificationCommand;
import com.twohands.notification_service.application.delivery.ApplySkipSelfNotificationUseCase;
import com.twohands.notification_service.application.inapp.CreateInAppNotificationCommand;
import com.twohands.notification_service.application.inapp.CreateInAppNotificationUseCase;
import com.twohands.notification_service.application.push.PushNotificationHandlerSupport;
import com.twohands.notification_service.application.push.SendPushNotificationCommand;
import com.twohands.notification_service.application.push.SendPushNotificationOutcome;
import com.twohands.notification_service.application.push.SendPushNotificationResult;
import com.twohands.notification_service.application.push.SendPushNotificationUseCase;
import com.twohands.notification_service.application.worker.NotificationFailurePolicy;
import com.twohands.notification_service.domain.delivery.NotificationDeliveryDecision;
import com.twohands.notification_service.domain.delivery.SkipSelfNotificationOutcome;
import com.twohands.notification_service.domain.notificationevent.NotificationEvent;
import com.twohands.notification_service.domain.social.UserFollowedNotificationContext;
import com.twohands.notification_service.exception.AppException;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;

@Component
@Order(30)
public class UserFollowedNotificationEventHandler implements NotificationEventHandler {

    private static final String USER_FOLLOWED = "USER_FOLLOWED";
    private static final String REFERENCE_TYPE = "USER";

    private final UserFollowedNotificationPayloadParser payloadParser;
    private final ApplySkipSelfNotificationUseCase applySkipSelfNotificationUseCase;
    private final ApplyNotificationDeliveryRulesUseCase applyNotificationDeliveryRulesUseCase;
    private final CreateInAppNotificationUseCase createInAppNotificationUseCase;
    private final SendPushNotificationUseCase sendPushNotificationUseCase;

    public UserFollowedNotificationEventHandler(
            UserFollowedNotificationPayloadParser payloadParser,
            ApplySkipSelfNotificationUseCase applySkipSelfNotificationUseCase,
            ApplyNotificationDeliveryRulesUseCase applyNotificationDeliveryRulesUseCase,
            CreateInAppNotificationUseCase createInAppNotificationUseCase,
            SendPushNotificationUseCase sendPushNotificationUseCase
    ) {
        this.payloadParser = payloadParser;
        this.applySkipSelfNotificationUseCase = applySkipSelfNotificationUseCase;
        this.applyNotificationDeliveryRulesUseCase = applyNotificationDeliveryRulesUseCase;
        this.createInAppNotificationUseCase = createInAppNotificationUseCase;
        this.sendPushNotificationUseCase = sendPushNotificationUseCase;
    }

    @Override
    public boolean supports(String eventType) {
        return USER_FOLLOWED.equals(eventType);
    }

    @Override
    public NotificationEventHandlerResult handle(NotificationEvent event) {
        UserFollowedNotificationContext context;
        try {
            context = payloadParser.parse(event);
        } catch (IllegalArgumentException ex) {
            return NotificationEventHandlerResult.failure(ex.getMessage(), NotificationFailurePolicy.PERMANENT);
        }

        if (context.actorId() == null) {
            return NotificationEventHandlerResult.failure(
                    "Actor id is required for USER_FOLLOWED notification event",
                    NotificationFailurePolicy.RETRYABLE
            );
        }

        SkipSelfNotificationOutcome skipOutcome = applySkipSelfNotificationUseCase.execute(
                new ApplySkipSelfNotificationCommand(
                        event.eventType(),
                        event.sourceService(),
                        context.actorId(),
                        context.followedUserId()
                )
        );
        if (skipOutcome == SkipSelfNotificationOutcome.SKIP) {
            return NotificationEventHandlerResult.noOp();
        }
        if (skipOutcome == SkipSelfNotificationOutcome.MISSING_ACTOR) {
            return NotificationEventHandlerResult.failure(
                    "Actor id is required for USER_FOLLOWED notification event",
                    NotificationFailurePolicy.RETRYABLE
            );
        }

        NotificationDeliveryDecision deliveryDecision;
        try {
            deliveryDecision = applyNotificationDeliveryRulesUseCase.execute(
                    new ApplyNotificationDeliveryRulesCommand(context.followedUserId(), USER_FOLLOWED)
            );
        } catch (DataAccessException ex) {
            return NotificationEventHandlerResult.failure(
                    "Failed to load notification delivery settings",
                    NotificationFailurePolicy.RETRYABLE
            );
        }

        String referenceId = context.actorId().toString();
        boolean delivered = false;

        if (deliveryDecision.inApp()) {
            try {
                createInAppNotificationUseCase.execute(new CreateInAppNotificationCommand(
                        event.id(),
                        context.followedUserId(),
                        context.actorId(),
                        USER_FOLLOWED,
                        REFERENCE_TYPE,
                        referenceId,
                        event.payload()
                ));
                delivered = true;
            } catch (AppException ex) {
                return NotificationEventHandlerResult.failure(
                        ex.getMessage(),
                        resolveFailurePolicy(ex)
                );
            }
        }

        if (deliveryDecision.push()) {
            SendPushNotificationResult pushResult = sendPushNotificationUseCase.execute(
                    new SendPushNotificationCommand(
                            context.followedUserId(),
                            USER_FOLLOWED,
                            REFERENCE_TYPE,
                            referenceId,
                            event.id()
                    )
            );

            var pushFailure = PushNotificationHandlerSupport.mapFailure(pushResult);
            if (pushFailure.isPresent()) {
                return pushFailure.get();
            }
            if (pushResult.outcome() == SendPushNotificationOutcome.SENT) {
                delivered = true;
            }
        }

        if (!delivered) {
            return NotificationEventHandlerResult.noOp();
        }

        return NotificationEventHandlerResult.success();
    }

    private NotificationFailurePolicy resolveFailurePolicy(AppException ex) {
        return switch (ex.getErrorCode()) {
            case UNKNOWN_EVENT_TYPE, INVALID_EVENT_PAYLOAD, VALIDATION_ERROR -> NotificationFailurePolicy.PERMANENT;
            default -> NotificationFailurePolicy.RETRYABLE;
        };
    }
}
