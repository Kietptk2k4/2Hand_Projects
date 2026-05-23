package com.twohands.notification_service.application.handler;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Component
public class NotificationEventHandlerRegistry {

    private final List<NotificationEventHandler> handlers;

    public NotificationEventHandlerRegistry(List<NotificationEventHandler> handlers) {
        this.handlers = handlers.stream()
                .sorted(Comparator.comparingInt(this::orderValue))
                .toList();
    }

    public Optional<NotificationEventHandler> resolve(String eventType) {
        return handlers.stream()
                .filter(handler -> handler.supports(eventType))
                .findFirst();
    }

    private int orderValue(NotificationEventHandler handler) {
        Order order = handler.getClass().getAnnotation(Order.class);
        return order == null ? Integer.MAX_VALUE : order.value();
    }
}
