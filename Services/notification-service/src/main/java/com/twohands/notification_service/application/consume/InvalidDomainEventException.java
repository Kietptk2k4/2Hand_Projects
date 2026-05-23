package com.twohands.notification_service.application.consume;

public class InvalidDomainEventException extends RuntimeException {

    public InvalidDomainEventException(String message) {
        super(message);
    }

    public InvalidDomainEventException(String message, Throwable cause) {
        super(message, cause);
    }
}
