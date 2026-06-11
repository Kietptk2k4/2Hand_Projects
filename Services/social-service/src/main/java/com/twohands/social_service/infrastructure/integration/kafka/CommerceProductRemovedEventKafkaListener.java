package com.twohands.social_service.infrastructure.integration.kafka;

import com.twohands.social_service.application.integration.handlecommerceproductremoved.CommerceProductRemovedEventMessageParser;
import com.twohands.social_service.application.integration.handlecommerceproductremoved.HandleCommerceProductRemovedUseCase;
import com.twohands.social_service.application.integration.handlecommerceproductremoved.InvalidCommerceProductRemovedEventException;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "social.kafka.consumer", name = "enabled", havingValue = "true")
public class CommerceProductRemovedEventKafkaListener {

    private static final Logger log = LoggerFactory.getLogger(CommerceProductRemovedEventKafkaListener.class);

    private final CommerceProductRemovedEventMessageParser messageParser;
    private final HandleCommerceProductRemovedUseCase handleCommerceProductRemovedUseCase;

    public CommerceProductRemovedEventKafkaListener(
            CommerceProductRemovedEventMessageParser messageParser,
            HandleCommerceProductRemovedUseCase handleCommerceProductRemovedUseCase
    ) {
        this.messageParser = messageParser;
        this.handleCommerceProductRemovedUseCase = handleCommerceProductRemovedUseCase;
    }

    @KafkaListener(
            topics = "#{@commerceProductRemovedEventTopics}",
            groupId = "${social.kafka.consumer.commerce-product-removed-group-id}",
            containerFactory = "authUserEventKafkaListenerContainerFactory"
    )
    public void onCommerceProductRemovedEvent(ConsumerRecord<String, String> record, Acknowledgment acknowledgment) {
        try {
            var command = messageParser.parse(record.value());
            handleCommerceProductRemovedUseCase.execute(command);
            if (acknowledgment != null) {
                acknowledgment.acknowledge();
            }
        } catch (InvalidCommerceProductRemovedEventException ex) {
            log.error(
                    "Invalid commerce product removed event payload. topic={}, partition={}, offset={}, error={}",
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
