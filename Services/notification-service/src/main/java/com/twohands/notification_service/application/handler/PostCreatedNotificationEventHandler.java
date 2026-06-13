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
import com.twohands.notification_service.domain.social.PostCreatedNotificationContext;
import com.twohands.notification_service.exception.AppException;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@Order(33)
public class PostCreatedNotificationEventHandler implements NotificationEventHandler {

    private static final String POST_CREATED = "POST_CREATED";
    private static final String REFERENCE_TYPE = "POST";

    private final PostCreatedNotificationPayloadParser payloadParser;
    private final ApplySkipSelfNotificationUseCase applySkipSelfNotificationUseCase;
    private final ApplyNotificationDeliveryRulesUseCase applyNotificationDeliveryRulesUseCase;
    private final CreateInAppNotificationUseCase createInAppNotificationUseCase;
    private final SendPushNotificationUseCase sendPushNotificationUseCase;

    public PostCreatedNotificationEventHandler(
            PostCreatedNotificationPayloadParser payloadParser,
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
        return POST_CREATED.equals(eventType);
    }

    @Override
    public NotificationEventHandlerResult handle(NotificationEvent event) {
        PostCreatedNotificationContext context;
        try {
            context = payloadParser.parse(event);
        } catch (IllegalArgumentException ex) {
            return NotificationEventHandlerResult.failure(ex.getMessage(), NotificationFailurePolicy.PERMANENT);
        }

        boolean delivered = false;
        for (UUID followerId : context.followerUserIds()) {
            RecipientDeliveryResult recipientResult = notifyFollower(event, context, followerId);
            if (recipientResult.failure().isPresent()) {
                return recipientResult.failure().get();
            }
            if (recipientResult.delivered()) {
                delivered = true;
            }
        }

        if (!delivered) {
            return NotificationEventHandlerResult.noOp();
        }
        return NotificationEventHandlerResult.success();
    }

    private RecipientDeliveryResult notifyFollower(
            NotificationEvent event,
            PostCreatedNotificationContext context,
            UUID followerId
    ) {
        SkipSelfNotificationOutcome skipOutcome = applySkipSelfNotificationUseCase.execute(
                new ApplySkipSelfNotificationCommand(
                        event.eventType(),
                        event.sourceService(),
                        context.actorId(),
                        followerId
                )
        );
        if (skipOutcome == SkipSelfNotificationOutcome.SKIP) {
            return RecipientDeliveryResult.skipped();
        }
        if (skipOutcome == SkipSelfNotificationOutcome.MISSING_ACTOR) {
            return RecipientDeliveryResult.failed(
                    NotificationEventHandlerResult.failure(
                            "Actor id is required for POST_CREATED notification event",
                            NotificationFailurePolicy.RETRYABLE
                    )
            );
        }

        NotificationDeliveryDecision deliveryDecision;
        try {
            deliveryDecision = applyNotificationDeliveryRulesUseCase.execute(
                    new ApplyNotificationDeliveryRulesCommand(followerId, POST_CREATED)
            );
        } catch (DataAccessException ex) {
            return RecipientDeliveryResult.failed(
                    NotificationEventHandlerResult.failure(
                            "Failed to load notification delivery settings",
                            NotificationFailurePolicy.RETRYABLE
                    )
            );
        }

        boolean delivered = false;

        if (deliveryDecision.inApp()) {
            try {
                createInAppNotificationUseCase.execute(new CreateInAppNotificationCommand(
                        event.id(),
                        followerId,
                        context.actorId(),
                        POST_CREATED,
                        REFERENCE_TYPE,
                        context.postId(),
                        event.payload(),
                        null,
                        context.actorDisplayName()
                ));
                delivered = true;
            } catch (AppException ex) {
                return RecipientDeliveryResult.failed(
                        NotificationEventHandlerResult.failure(
                                ex.getMessage(),
                                resolveFailurePolicy(ex)
                        )
                );
            }
        }

        if (deliveryDecision.push()) {
            SendPushNotificationResult pushResult = sendPushNotificationUseCase.execute(
                    new SendPushNotificationCommand(
                            followerId,
                            POST_CREATED,
                            REFERENCE_TYPE,
                            context.postId(),
                            event.id(),
                            null,
                            context.actorDisplayName()
                    )
            );

            var pushFailure = PushNotificationHandlerSupport.mapFailure(pushResult);
            if (pushFailure.isPresent()) {
                return RecipientDeliveryResult.failed(pushFailure.get());
            }
            if (pushResult.outcome() == SendPushNotificationOutcome.SENT) {
                delivered = true;
            }
        }

        return RecipientDeliveryResult.of(delivered);
    }

    private NotificationFailurePolicy resolveFailurePolicy(AppException ex) {
        return switch (ex.getErrorCode()) {
            case UNKNOWN_EVENT_TYPE, INVALID_EVENT_PAYLOAD, VALIDATION_ERROR -> NotificationFailurePolicy.PERMANENT;
            default -> NotificationFailurePolicy.RETRYABLE;
        };
    }

    private record RecipientDeliveryResult(boolean delivered, Optional<NotificationEventHandlerResult> failure) {
        static RecipientDeliveryResult skipped() {
            return new RecipientDeliveryResult(false, Optional.empty());
        }

        static RecipientDeliveryResult of(boolean delivered) {
            return new RecipientDeliveryResult(delivered, Optional.empty());
        }

        static RecipientDeliveryResult failed(NotificationEventHandlerResult failure) {
            return new RecipientDeliveryResult(false, Optional.of(failure));
        }
    }
}
