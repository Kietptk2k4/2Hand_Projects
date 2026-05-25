package com.twohands.notification_service.domain.email;

public interface EmailNotificationProvider {

    EmailProviderSendResult send(EmailNotificationContent content) throws EmailProviderException;
}
