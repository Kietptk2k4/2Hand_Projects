package com.twohands.social_service.infrastructure.integration.kafka;

import com.twohands.social_service.application.integration.consumeauthuserevents.AuthUserEventMessageParser;
import com.twohands.social_service.application.integration.consumeauthuserevents.ConsumeAuthUserEventsUseCase;
import com.twohands.social_service.application.integration.consumeauthuserevents.InvalidAuthUserEventException;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "social.kafka.consumer", name = "enabled", havingValue = "true")
public class AuthUserEventKafkaListener {

    private static final Logger log = LoggerFactory.getLogger(AuthUserEventKafkaListener.class);

    private final AuthUserEventMessageParser messageParser;
    private final AuthUserEventTopicResolver topicResolver;
    private final ConsumeAuthUserEventsUseCase consumeAuthUserEventsUseCase;

    public AuthUserEventKafkaListener(
            AuthUserEventMessageParser messageParser,
            AuthUserEventTopicResolver topicResolver,
            ConsumeAuthUserEventsUseCase consumeAuthUserEventsUseCase
    ) {
        this.messageParser = messageParser;
        this.topicResolver = topicResolver;
        this.consumeAuthUserEventsUseCase = consumeAuthUserEventsUseCase;
    }

    @KafkaListener(
            topics = "#{@authUserEventTopics}",
            groupId = "${social.kafka.consumer.group-id}",
            containerFactory = "authUserEventKafkaListenerContainerFactory"
    )
    public void onAuthUserEvent(ConsumerRecord<String, String> record, Acknowledgment acknowledgment) {
        String topic = record.topic();
        String fallbackEventType = topicResolver.resolveEventType(topic);

        try {
            var command = messageParser.parse(record.value(), topic, fallbackEventType);
            consumeAuthUserEventsUseCase.execute(command);
            if (acknowledgment != null) {
                acknowledgment.acknowledge();
            }
        } catch (InvalidAuthUserEventException ex) {
            log.error(
                    "Invalid auth user event payload. topic={}, partition={}, offset={}, error={}",
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
