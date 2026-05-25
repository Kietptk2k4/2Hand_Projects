package com.twohands.notification_service.domain.email;

public class EmailProviderException extends Exception {

    private final EmailDeliveryFailureType failureType;

    public EmailProviderException(EmailDeliveryFailureType failureType, String message) {
        super(message);
        this.failureType = failureType;
    }

    public EmailProviderException(EmailDeliveryFailureType failureType, String message, Throwable cause) {
        super(message, cause);
        this.failureType = failureType;
    }

    public EmailDeliveryFailureType failureType() {
        return failureType;
    }
}
