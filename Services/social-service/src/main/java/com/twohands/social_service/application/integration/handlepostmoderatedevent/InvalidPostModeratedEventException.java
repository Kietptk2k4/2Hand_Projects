package com.twohands.social_service.application.integration.handlepostmoderatedevent;

public class InvalidPostModeratedEventException extends RuntimeException {

    public InvalidPostModeratedEventException(String message) {
        super(message);
    }
}
