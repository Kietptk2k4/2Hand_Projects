package com.twohands.admin_service.infrastructure.outbox;

import com.twohands.admin_service.application.outbox.OutboxEventPublisher;
import com.twohands.admin_service.domain.outbox.OutboxEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "admin.kafka.producer", name = "enabled", havingValue = "false", matchIfMissing = true)
public class LoggingOutboxEventPublisher implements OutboxEventPublisher {

	private static final Logger log = LoggerFactory.getLogger(LoggingOutboxEventPublisher.class);

	private final AdminOutboxTopicResolver topicResolver;
	private final AdminOutboxMessageBuilder messageBuilder;
	private final AdminOutboxEventKeyResolver eventKeyResolver;
	private final OutboxPublishPayloadGuard payloadGuard;

	public LoggingOutboxEventPublisher(
			AdminOutboxTopicResolver topicResolver,
			AdminOutboxMessageBuilder messageBuilder,
			AdminOutboxEventKeyResolver eventKeyResolver,
			OutboxPublishPayloadGuard payloadGuard
	) {
		this.topicResolver = topicResolver;
		this.messageBuilder = messageBuilder;
		this.eventKeyResolver = eventKeyResolver;
		this.payloadGuard = payloadGuard;
	}

	@Override
	public void publish(OutboxEvent event) {
		payloadGuard.assertSafeToPublish(event.payload());
		String topic = topicResolver.resolve(event.eventType());
		String eventKey = eventKeyResolver.resolve(event.eventType(), event.aggregateId());
		String envelopeJson = messageBuilder.buildEnvelopeJson(event);
		log.info(
				"Outbox publish stub invoked. outboxEventId={}, eventType={}, eventKey={}, topic={}, aggregateId={}, source=admin, envelopeBytes={}",
				event.id(),
				event.eventType(),
				eventKey,
				topic,
				event.aggregateId(),
				envelopeJson.length()
		);
		log.debug("Outbox publish envelope. topic={}, envelope={}", topic, envelopeJson);
	}
}
