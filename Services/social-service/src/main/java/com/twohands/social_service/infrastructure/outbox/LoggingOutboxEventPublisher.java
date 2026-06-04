package com.twohands.social_service.infrastructure.outbox;

import com.twohands.social_service.application.outbox.OutboxEventPublisher;
import com.twohands.social_service.domain.outbox.OutboxEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "social.kafka.producer", name = "enabled", havingValue = "false", matchIfMissing = true)
public class LoggingOutboxEventPublisher implements OutboxEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(LoggingOutboxEventPublisher.class);

    private final SocialOutboxTopicResolver topicResolver;
    private final SocialOutboxMessageBuilder messageBuilder;

    public LoggingOutboxEventPublisher(
            SocialOutboxTopicResolver topicResolver,
            SocialOutboxMessageBuilder messageBuilder
    ) {
        this.topicResolver = topicResolver;
        this.messageBuilder = messageBuilder;
    }

    @Override
    public void publish(OutboxEvent event) {
        String topic = topicResolver.resolve(event.eventType());
        String envelopeJson = messageBuilder.buildEnvelopeJson(event);
        log.info(
                "Outbox publish stub invoked. outboxEventId={}, eventType={}, topic={}, aggregateId={}, envelopeBytes={}",
                event.id(),
                event.eventType(),
                topic,
                event.aggregateId(),
                envelopeJson.length()
        );
    }
}
