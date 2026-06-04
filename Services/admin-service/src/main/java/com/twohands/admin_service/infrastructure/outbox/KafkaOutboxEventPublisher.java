package com.twohands.admin_service.infrastructure.outbox;

import com.twohands.admin_service.application.outbox.OutboxEventPublisher;
import com.twohands.admin_service.config.AdminKafkaProducerProperties;
import com.twohands.admin_service.domain.outbox.OutboxEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@ConditionalOnProperty(prefix = "admin.kafka.producer", name = "enabled", havingValue = "true")
public class KafkaOutboxEventPublisher implements OutboxEventPublisher {

	private static final Logger log = LoggerFactory.getLogger(KafkaOutboxEventPublisher.class);

	private final KafkaTemplate<String, String> kafkaTemplate;
	private final AdminOutboxTopicResolver topicResolver;
	private final AdminOutboxMessageBuilder messageBuilder;
	private final AdminOutboxEventKeyResolver eventKeyResolver;
	private final OutboxPublishPayloadGuard payloadGuard;
	private final AdminKafkaProducerProperties producerProperties;

	public KafkaOutboxEventPublisher(
			KafkaTemplate<String, String> kafkaTemplate,
			AdminOutboxTopicResolver topicResolver,
			AdminOutboxMessageBuilder messageBuilder,
			AdminOutboxEventKeyResolver eventKeyResolver,
			OutboxPublishPayloadGuard payloadGuard,
			AdminKafkaProducerProperties producerProperties
	) {
		this.kafkaTemplate = kafkaTemplate;
		this.topicResolver = topicResolver;
		this.messageBuilder = messageBuilder;
		this.eventKeyResolver = eventKeyResolver;
		this.payloadGuard = payloadGuard;
		this.producerProperties = producerProperties;
	}

	@Override
	public void publish(OutboxEvent event) {
		payloadGuard.assertSafeToPublish(event.payload());
		String topic = topicResolver.resolve(event.eventType());
		String eventKey = eventKeyResolver.resolve(event.eventType(), event.aggregateId());
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
