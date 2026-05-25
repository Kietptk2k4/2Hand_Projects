package com.twohands.notification_service.domain.push;

import com.twohands.notification_service.domain.devicetoken.UserDeviceToken;

public interface PushNotificationProvider {

    PushProviderSendResult send(PushNotificationPayload payload, UserDeviceToken deviceToken) throws PushProviderException;
}
