package com.twohands.notification_service.infrastructure.push;

import com.twohands.notification_service.domain.devicetoken.RegisterDeviceTokenPolicy;
import com.twohands.notification_service.domain.devicetoken.UserDeviceToken;
import com.twohands.notification_service.domain.push.PushDeliveryFailureType;
import com.twohands.notification_service.domain.push.PushNotificationPayload;
import com.twohands.notification_service.domain.push.PushNotificationProvider;
import com.twohands.notification_service.domain.push.PushProviderException;
import com.twohands.notification_service.domain.push.PushProviderSendResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class LoggingFcmPushNotificationProvider implements PushNotificationProvider {

    private static final Logger log = LoggerFactory.getLogger(LoggingFcmPushNotificationProvider.class);

    @Override
    public PushProviderSendResult send(PushNotificationPayload payload, UserDeviceToken deviceToken)
            throws PushProviderException {
        if (payload.title() == null || payload.title().isBlank()) {
            throw new PushProviderException(PushDeliveryFailureType.PERMANENT, "Push title is required.");
        }
        if (payload.body() == null || payload.body().isBlank()) {
            throw new PushProviderException(PushDeliveryFailureType.PERMANENT, "Push body is required.");
        }

        String token = deviceToken.deviceToken();
        if (token != null && token.contains("invalid-token")) {
            throw new PushProviderException(PushDeliveryFailureType.INVALID_TOKEN, "FCM token is invalid or unregistered.");
        }
        if (token != null && token.contains("retryable-token")) {
            throw new PushProviderException(PushDeliveryFailureType.RETRYABLE, "FCM provider timeout.");
        }

        String messageId = UUID.randomUUID().toString();
        log.info(
                "FCM provider accepted push messageId={} token={} title={}",
                messageId,
                RegisterDeviceTokenPolicy.maskDeviceToken(token),
                payload.title()
        );
        return new PushProviderSendResult(messageId);
    }
}
