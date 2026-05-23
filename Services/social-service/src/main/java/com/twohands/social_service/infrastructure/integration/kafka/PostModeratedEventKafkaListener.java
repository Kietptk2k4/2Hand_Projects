package com.twohands.social_service.infrastructure.integration.kafka;

import com.twohands.social_service.application.integration.handlepostmoderatedevent.HandlePostModeratedEventUseCase;
import com.twohands.social_service.application.integration.handlepostmoderatedevent.InvalidPostModeratedEventException;
import com.twohands.social_service.application.integration.handlepostmoderatedevent.PostModeratedEventMessageParser;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "social.kafka.consumer", name = "enabled", havingValue = "true")
public class PostModeratedEventKafkaListener {

    private static final Logger log = LoggerFactory.getLogger(PostModeratedEventKafkaListener.class);

    private final PostModeratedEventMessageParser messageParser;
    private final HandlePostModeratedEventUseCase handlePostModeratedEventUseCase;

    public PostModeratedEventKafkaListener(
            PostModeratedEventMessageParser messageParser,
            HandlePostModeratedEventUseCase handlePostModeratedEventUseCase
    ) {
        this.messageParser = messageParser;
        this.handlePostModeratedEventUseCase = handlePostModeratedEventUseCase;
    }

    @KafkaListener(
            topics = "#{@postModeratedEventTopics}",
            groupId = "${social.kafka.consumer.post-moderated-group-id}",
            containerFactory = "authUserEventKafkaListenerContainerFactory"
    )
    public void onPostModeratedEvent(ConsumerRecord<String, String> record, Acknowledgment acknowledgment) {
        try {
            var command = messageParser.parse(record.value());
            handlePostModeratedEventUseCase.execute(command);
            if (acknowledgment != null) {
                acknowledgment.acknowledge();
            }
        } catch (InvalidPostModeratedEventException ex) {
            log.error(
                    "Invalid post moderated event payload. topic={}, partition={}, offset={}, error={}",
                    record.topic(),
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
