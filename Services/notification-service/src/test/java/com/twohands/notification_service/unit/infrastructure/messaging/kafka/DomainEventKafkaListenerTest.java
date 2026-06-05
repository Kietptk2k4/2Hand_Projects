package com.twohands.notification_service.unit.infrastructure.messaging.kafka;

import com.twohands.notification_service.application.consume.ConsumeDomainEventResult;
import com.twohands.notification_service.application.consume.ConsumeDomainEventUseCase;
import com.twohands.notification_service.application.consume.InvalidDomainEventException;
import com.twohands.notification_service.infrastructure.messaging.kafka.DomainEventKafkaListener;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.support.Acknowledgment;

import java.util.UUID;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DomainEventKafkaListenerTest {

    @Mock
    private ConsumeDomainEventUseCase consumeDomainEventUseCase;

    @Mock
    private Acknowledgment acknowledgment;

    @InjectMocks
    private DomainEventKafkaListener listener;

    @Test
    void onDomainEvent_acknowledgesAfterSuccessfulIngest() {
        ConsumerRecord<String, String> record = new ConsumerRecord<>("social.post.liked", 0, 12L, "key", "{}");
        when(consumeDomainEventUseCase.execute("{}", "social.post.liked"))
                .thenReturn(new ConsumeDomainEventResult(UUID.randomUUID(), false));

        listener.onDomainEvent(record, acknowledgment);

        verify(acknowledgment).acknowledge();
        verify(consumeDomainEventUseCase).execute("{}", "social.post.liked");
    }

    @Test
    void onDomainEvent_acknowledgesInvalidPayloadWithoutCrashingLoop() {
        ConsumerRecord<String, String> record = new ConsumerRecord<>("social.post.liked", 0, 13L, "key", "{bad");
        when(consumeDomainEventUseCase.execute("{bad", "social.post.liked"))
                .thenThrow(new InvalidDomainEventException("Cannot parse domain event message"));

        listener.onDomainEvent(record, acknowledgment);

        verify(acknowledgment).acknowledge();
    }

    @Test
    void onDomainEvent_doesNotAcknowledgeWhenIngestFailsWithTransientError() {
        ConsumerRecord<String, String> record = new ConsumerRecord<>("social.post.liked", 0, 14L, "key", "{}");
        when(consumeDomainEventUseCase.execute("{}", "social.post.liked"))
                .thenThrow(new RuntimeException("db unavailable"));

        org.junit.jupiter.api.Assertions.assertThrows(
                RuntimeException.class,
                () -> listener.onDomainEvent(record, acknowledgment)
        );
        verify(acknowledgment, never()).acknowledge();
    }
}
