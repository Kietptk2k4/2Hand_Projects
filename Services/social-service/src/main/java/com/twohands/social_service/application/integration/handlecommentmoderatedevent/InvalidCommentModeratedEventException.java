package com.twohands.social_service.application.integration.handlecommentmoderatedevent;

public class InvalidCommentModeratedEventException extends RuntimeException {

    public InvalidCommentModeratedEventException(String message) {
        super(message);
    }
}
