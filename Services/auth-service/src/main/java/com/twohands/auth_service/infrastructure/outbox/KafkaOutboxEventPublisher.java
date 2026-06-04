package com.twohands.auth_service.infrastructure.outbox;

import com.twohands.auth_service.application.outbox.OutboxEventPublisher;
import com.twohands.auth_service.config.AuthKafkaProducerProperties;
import com.twohands.auth_service.domain.outbox.OutboxEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@ConditionalOnProperty(prefix = "auth.kafka.producer", name = "enabled", havingValue = "true")
public class KafkaOutboxEventPublisher implements OutboxEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(KafkaOutboxEventPublisher.class);

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final AuthOutboxTopicResolver topicResolver;
    private final AuthOutboxMessageBuilder messageBuilder;
    private final AuthOutboxEventKeyResolver eventKeyResolver;
    private final AuthKafkaProducerProperties producerProperties;

    public KafkaOutboxEventPublisher(
            KafkaTemplate<String, String> kafkaTemplate,
            AuthOutboxTopicResolver topicResolver,
            AuthOutboxMessageBuilder messageBuilder,
            AuthOutboxEventKeyResolver eventKeyResolver,
            AuthKafkaProducerProperties producerProperties
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.topicResolver = topicResolver;
        this.messageBuilder = messageBuilder;
        this.eventKeyResolver = eventKeyResolver;
        this.producerProperties = producerProperties;
    }

    @Override
    public void publish(OutboxEvent event) {
        String topic = topicResolver.resolve(event.eventType());
        String eventKey = eventKeyResolver.resolve(event);
        String envelopeJson = messageBuilder.buildEnvelopeJson(event);

        try {
            kafkaTemplate.send(topic, eventKey, envelopeJson)
                    .get(producerProperties.getSendTimeoutMs(), TimeUnit.MILLISECONDS);
            log.info(
                    "Outbox event published to Kafka. outboxEventId={}, eventType={}, topic={}",
                    event.id(),
                    event.eventType(),
                    topic
            );
        } catch (Exception ex) {
            throw new IllegalStateException(
                    "Failed to publish outbox event to Kafka: " + ex.getMessage(),
                    ex
            );
        }
    }
}
