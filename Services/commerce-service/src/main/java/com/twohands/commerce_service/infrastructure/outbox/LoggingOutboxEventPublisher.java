package com.twohands.commerce_service.infrastructure.outbox;

import com.twohands.commerce_service.application.outbox.OutboxEventPublisher;
import com.twohands.commerce_service.domain.outbox.OutboxEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class LoggingOutboxEventPublisher implements OutboxEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(LoggingOutboxEventPublisher.class);

    private final CommerceOutboxTopicResolver topicResolver;
    private final CommerceOutboxMessageBuilder messageBuilder;

    public LoggingOutboxEventPublisher(
            CommerceOutboxTopicResolver topicResolver,
            CommerceOutboxMessageBuilder messageBuilder
    ) {
        this.topicResolver = topicResolver;
        this.messageBuilder = messageBuilder;
    }

    @Override
    public void publish(OutboxEvent event) {
        String topic = topicResolver.resolve(event.eventType());
        String envelopeJson = messageBuilder.buildEnvelopeJson(event);
        log.info(
                "Outbox publish stub invoked. outboxEventId={}, eventType={}, eventKey={}, topic={}, aggregateId={}, source={}, envelopeBytes={}",
                event.id(),
                event.eventType(),
                event.eventKey(),
                topic,
                event.aggregateId(),
                event.source(),
                envelopeJson.length()
        );
        log.debug("Outbox publish envelope. topic={}, envelope={}", topic, envelopeJson);
    }
}
