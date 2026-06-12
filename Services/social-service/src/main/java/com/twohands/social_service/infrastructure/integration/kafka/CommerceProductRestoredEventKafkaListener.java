package com.twohands.social_service.infrastructure.integration.kafka;

import com.twohands.social_service.application.integration.handlecommerceproductremoved.CommerceProductRemovedEventMessageParser;
import com.twohands.social_service.application.integration.handlecommerceproductremoved.InvalidCommerceProductRemovedEventException;
import com.twohands.social_service.application.integration.handlecommerceproductrestored.HandleCommerceProductRestoredUseCase;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "social.kafka.consumer", name = "enabled", havingValue = "true")
public class CommerceProductRestoredEventKafkaListener {

    private static final Logger log = LoggerFactory.getLogger(CommerceProductRestoredEventKafkaListener.class);

    private final CommerceProductRemovedEventMessageParser messageParser;
    private final HandleCommerceProductRestoredUseCase handleCommerceProductRestoredUseCase;

    public CommerceProductRestoredEventKafkaListener(
            CommerceProductRemovedEventMessageParser messageParser,
            HandleCommerceProductRestoredUseCase handleCommerceProductRestoredUseCase
    ) {
        this.messageParser = messageParser;
        this.handleCommerceProductRestoredUseCase = handleCommerceProductRestoredUseCase;
    }

    @KafkaListener(
            topics = "#{@commerceProductRestoredEventTopics}",
            groupId = "${social.kafka.consumer.commerce-product-restored-group-id}",
            containerFactory = "authUserEventKafkaListenerContainerFactory"
    )
    public void onCommerceProductRestoredEvent(ConsumerRecord<String, String> record, Acknowledgment acknowledgment) {
        try {
            var command = messageParser.parse(record.value());
            handleCommerceProductRestoredUseCase.execute(command);
            if (acknowledgment != null) {
                acknowledgment.acknowledge();
            }
        } catch (InvalidCommerceProductRemovedEventException ex) {
            log.error(
                    "Invalid commerce product restored event payload. topic={}, partition={}, offset={}, error={}",
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
