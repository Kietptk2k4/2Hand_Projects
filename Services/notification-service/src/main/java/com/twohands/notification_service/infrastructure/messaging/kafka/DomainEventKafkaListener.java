package com.twohands.notification_service.infrastructure.messaging.kafka;

import com.twohands.notification_service.application.consume.ConsumeDomainEventUseCase;
import com.twohands.notification_service.application.consume.InvalidDomainEventException;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "notification.kafka.consumer", name = "enabled", havingValue = "true")
public class DomainEventKafkaListener {

    private static final Logger log = LoggerFactory.getLogger(DomainEventKafkaListener.class);

    private final ConsumeDomainEventUseCase consumeDomainEventUseCase;

    public DomainEventKafkaListener(ConsumeDomainEventUseCase consumeDomainEventUseCase) {
        this.consumeDomainEventUseCase = consumeDomainEventUseCase;
    }

    @KafkaListener(
            topics = "#{@domainEventTopics}",
            groupId = "${notification.kafka.consumer.group-id}",
            containerFactory = "domainEventKafkaListenerContainerFactory"
    )
    public void onDomainEvent(ConsumerRecord<String, String> record, Acknowledgment acknowledgment) {
        String topic = record.topic();
        try {
            var result = consumeDomainEventUseCase.execute(record.value(), topic);
            if (acknowledgment != null) {
                acknowledgment.acknowledge();
            }
            if (result.duplicate()) {
                log.debug(
                        "Duplicate domain event acknowledged. topic={}, partition={}, offset={}, notificationEventId={}",
                        topic,
                        record.partition(),
                        record.offset(),
                        result.notificationEventId()
                );
            }
        } catch (InvalidDomainEventException ex) {
            log.warn(
                    "Invalid domain event payload acknowledged. topic={}, partition={}, offset={}, error={}",
                    topic,
                    record.partition(),
                    record.offset(),
                    ex.getMessage()
            );
            if (acknowledgment != null) {
                acknowledgment.acknowledge();
            }
        }
    }
}
