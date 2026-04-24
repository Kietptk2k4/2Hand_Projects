package com.twohands.authservice.infrastructure.message.kafka;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.twohands.authservice.application.auth.event.OutboxRecord;
import com.twohands.authservice.application.auth.port.OutboxRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Component
public class OutboxPublisherScheduler {

    private static final Logger log = LoggerFactory.getLogger(OutboxPublisherScheduler.class);
    private static final String TOPIC = "system.events";
    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {};

    private final OutboxRepository outboxRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public OutboxPublisherScheduler(OutboxRepository outboxRepository,
                                    KafkaTemplate<String, Object> kafkaTemplate,
                                    ObjectMapper objectMapper) {
        this.outboxRepository = outboxRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    @Scheduled(fixedDelayString = "${auth.outbox.poll-delay-ms:5000}")
    @Transactional
    public void publishPendingEvents() {
        List<OutboxRecord> pending = outboxRepository.findPending();

        for (OutboxRecord record : pending) {
            try {
                Map<String, Object> data = objectMapper.readValue(record.payload(), MAP_TYPE);
                EventEnvelope envelope = new EventEnvelope(
                        record.id().toString(),
                        record.eventType(),
                        java.time.Instant.now().toString(),
                        record.source(),
                        data
                );

                kafkaTemplate.send(TOPIC, record.id().toString(), envelope)
                        .get(10, java.util.concurrent.TimeUnit.SECONDS);

                outboxRepository.markPublished(record.id());
                log.info("Outbox event {} [{}] published successfully", record.id(), record.eventType());

            } catch (Exception ex) {
                log.error("Failed to publish outbox event {} [{}]: {}", record.id(), record.eventType(), ex.getMessage());
                outboxRepository.incrementRetry(record.id());
            }
        }
    }
}
