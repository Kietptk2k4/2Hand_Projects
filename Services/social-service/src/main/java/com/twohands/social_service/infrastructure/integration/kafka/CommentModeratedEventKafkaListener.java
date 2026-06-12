package com.twohands.social_service.infrastructure.integration.kafka;

import com.twohands.social_service.application.integration.handlecommentmoderatedevent.CommentModeratedEventMessageParser;
import com.twohands.social_service.application.integration.handlecommentmoderatedevent.HandleCommentModeratedEventUseCase;
import com.twohands.social_service.application.integration.handlecommentmoderatedevent.InvalidCommentModeratedEventException;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "social.kafka.consumer", name = "enabled", havingValue = "true")
public class CommentModeratedEventKafkaListener {

    private static final Logger log = LoggerFactory.getLogger(CommentModeratedEventKafkaListener.class);

    private final CommentModeratedEventMessageParser messageParser;
    private final HandleCommentModeratedEventUseCase handleCommentModeratedEventUseCase;

    public CommentModeratedEventKafkaListener(
            CommentModeratedEventMessageParser messageParser,
            HandleCommentModeratedEventUseCase handleCommentModeratedEventUseCase
    ) {
        this.messageParser = messageParser;
        this.handleCommentModeratedEventUseCase = handleCommentModeratedEventUseCase;
    }

    @KafkaListener(
            topics = "#{@commentModeratedEventTopics}",
            groupId = "${social.kafka.consumer.comment-moderated-group-id}",
            containerFactory = "authUserEventKafkaListenerContainerFactory"
    )
    public void onCommentModeratedEvent(ConsumerRecord<String, String> record, Acknowledgment acknowledgment) {
        try {
            var command = messageParser.parse(record.value());
            handleCommentModeratedEventUseCase.execute(command);
            if (acknowledgment != null) {
                acknowledgment.acknowledge();
            }
        } catch (InvalidCommentModeratedEventException ex) {
            log.error(
                    "Invalid comment moderated event payload. topic={}, partition={}, offset={}, error={}",
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
