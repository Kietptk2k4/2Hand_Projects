package com.twohands.social_service.application.integration.consumeauthuserevents;

public class InvalidAuthUserEventException extends RuntimeException {

    public InvalidAuthUserEventException(String message) {
        super(message);
    }
}
