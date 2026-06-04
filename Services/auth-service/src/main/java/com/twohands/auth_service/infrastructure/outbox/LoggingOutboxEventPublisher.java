package com.twohands.auth_service.infrastructure.outbox;

import com.twohands.auth_service.application.outbox.OutboxEventPublisher;
import com.twohands.auth_service.domain.outbox.OutboxEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "auth.kafka.producer", name = "enabled", havingValue = "false", matchIfMissing = true)
public class LoggingOutboxEventPublisher implements OutboxEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(LoggingOutboxEventPublisher.class);

    private final AuthOutboxTopicResolver topicResolver;
    private final AuthOutboxMessageBuilder messageBuilder;

    public LoggingOutboxEventPublisher(
            AuthOutboxTopicResolver topicResolver,
            AuthOutboxMessageBuilder messageBuilder
    ) {
        this.topicResolver = topicResolver;
        this.messageBuilder = messageBuilder;
    }

    @Override
    public void publish(OutboxEvent event) {
        String topic = topicResolver.resolve(event.eventType());
        String envelopeJson = messageBuilder.buildEnvelopeJson(event);
        log.info(
                "Outbox publish stub invoked. outboxEventId={}, eventType={}, topic={}, source={}, envelopeBytes={}",
                event.id(),
                event.eventType(),
                topic,
                event.source(),
                envelopeJson.length()
        );
    }
}
