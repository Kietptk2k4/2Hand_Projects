package com.twohands.auth_service.infrastructure.integration.kafka;

import com.twohands.auth_service.application.admin.applyuserenforcement.ConsumeUserEnforcementEventUseCase;
import com.twohands.auth_service.application.admin.applyuserenforcement.InvalidUserEnforcementEventException;
import com.twohands.auth_service.application.admin.applyuserenforcement.UserEnforcementEventMessageParser;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "auth.kafka.consumer", name = "enabled", havingValue = "true")
public class UserEnforcementEventKafkaListener {

    private static final Logger log = LoggerFactory.getLogger(UserEnforcementEventKafkaListener.class);

    private final UserEnforcementEventMessageParser messageParser;
    private final UserEnforcementEventTopicResolver topicResolver;
    private final ConsumeUserEnforcementEventUseCase consumeUserEnforcementEventUseCase;

    public UserEnforcementEventKafkaListener(
            UserEnforcementEventMessageParser messageParser,
            UserEnforcementEventTopicResolver topicResolver,
            ConsumeUserEnforcementEventUseCase consumeUserEnforcementEventUseCase
    ) {
        this.messageParser = messageParser;
        this.topicResolver = topicResolver;
        this.consumeUserEnforcementEventUseCase = consumeUserEnforcementEventUseCase;
    }

    @KafkaListener(
            topics = "#{@authUserEnforcementEventTopics}",
            groupId = "${auth.kafka.consumer.group-id}",
            containerFactory = "authUserEnforcementKafkaListenerContainerFactory"
    )
    public void onUserEnforcementEvent(ConsumerRecord<String, String> record, Acknowledgment acknowledgment) {
        String topic = record.topic();
        String fallbackEventType = topicResolver.resolveEventType(topic);

        try {
            var command = messageParser.parse(record.value(), topic, fallbackEventType);
            consumeUserEnforcementEventUseCase.execute(command);
            if (acknowledgment != null) {
                acknowledgment.acknowledge();
            }
        } catch (InvalidUserEnforcementEventException ex) {
            log.error(
                    "Invalid user enforcement event payload. topic={}, partition={}, offset={}, error={}",
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
