package com.twohands.notification_service.application.handler;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class NotificationEventHandlerRegistry {

    private final List<NotificationEventHandler> handlers;

    public NotificationEventHandlerRegistry(List<NotificationEventHandler> handlers) {
        this.handlers = handlers;
    }

    public Optional<NotificationEventHandler> resolve(String eventType) {
        return handlers.stream()
                .filter(handler -> handler.supports(eventType))
                .findFirst();
    }
}
