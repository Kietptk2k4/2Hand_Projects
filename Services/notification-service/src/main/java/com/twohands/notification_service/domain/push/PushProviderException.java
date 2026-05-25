package com.twohands.notification_service.domain.push;

public class PushProviderException extends Exception {

    private final PushDeliveryFailureType failureType;

    public PushProviderException(PushDeliveryFailureType failureType, String message) {
        super(message);
        this.failureType = failureType;
    }

    public PushProviderException(PushDeliveryFailureType failureType, String message, Throwable cause) {
        super(message, cause);
        this.failureType = failureType;
    }

    public PushDeliveryFailureType failureType() {
        return failureType;
    }
}
