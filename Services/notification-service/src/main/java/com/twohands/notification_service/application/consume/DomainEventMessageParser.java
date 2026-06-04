package com.twohands.notification_service.application.consume;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.twohands.notification_service.domain.notificationevent.NotificationEventTypeAliasResolver;
import com.twohands.notification_service.domain.notificationevent.NotificationSourceService;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;
import java.util.regex.Pattern;

@Component
public class DomainEventMessageParser {

    private static final Pattern EVENT_TYPE_PATTERN = Pattern.compile("^[A-Z][A-Z0-9_]*$");

    private final ObjectMapper objectMapper;
    private final DomainEventTopicResolver topicResolver;
    private final NotificationEventTypeAliasResolver eventTypeAliasResolver;

    public DomainEventMessageParser(
            ObjectMapper objectMapper,
            DomainEventTopicResolver topicResolver,
            NotificationEventTypeAliasResolver eventTypeAliasResolver
    ) {
        this.objectMapper = objectMapper;
        this.topicResolver = topicResolver;
        this.eventTypeAliasResolver = eventTypeAliasResolver;
    }

    public ConsumeDomainEventCommand parse(String rawMessage, String topic) {
        if (rawMessage == null || rawMessage.isBlank()) {
            throw new InvalidDomainEventException("Event message body is empty");
        }

        try {
            JsonNode root = objectMapper.readTree(rawMessage);
            JsonNode payload = root.hasNonNull("payload") && root.get("payload").isObject()
                    ? root.get("payload")
                    : root;

            UUID eventId = firstUuid(root, payload, "event_id");
            if (eventId == null) {
                eventId = uuid(root, "id");
            }
            if (eventId == null) {
                throw new InvalidDomainEventException("event_id is required");
            }

            String eventType = text(root, "event_type");
            if (eventType == null) {
                eventType = text(payload, "event_type");
            }
            if (eventType == null) {
                eventType = topicResolver.resolveFallbackEventType(topic);
            }
            if (eventType == null || eventType.isBlank()) {
                throw new InvalidDomainEventException("event_type is required");
            }

            String canonicalEventType = eventTypeAliasResolver.resolve(eventType);
            if (!EVENT_TYPE_PATTERN.matcher(canonicalEventType).matches()) {
                throw new InvalidDomainEventException("event_type must use UPPER_SNAKE_CASE");
            }

            NotificationSourceService sourceService = topicResolver.resolveSourceService(
                    topic,
                    text(root, "source_service")
            );

            String eventKey = text(root, "event_key");
            if (eventKey == null) {
                eventKey = text(payload, "event_key");
            }

            String aggregateType = text(root, "aggregate_type");
            if (aggregateType == null) {
                aggregateType = text(payload, "aggregate_type");
            }

            String aggregateId = text(root, "aggregate_id");
            if (aggregateId == null) {
                aggregateId = text(payload, "aggregate_id");
            }

            UUID actorId = firstUuid(root, payload, "actor_id");
            UUID recipientUserId = resolveRecipientUserId(root, payload);

            Instant occurredAt = instant(root, "occurred_at");
            if (occurredAt == null) {
                occurredAt = instant(payload, "occurred_at");
            }

            String payloadJson = serializePayload(payload);

            return new ConsumeDomainEventCommand(
                    eventId,
                    canonicalEventType,
                    sourceService,
                    eventKey,
                    aggregateType,
                    aggregateId,
                    actorId,
                    recipientUserId,
                    payloadJson,
                    occurredAt
            );
        } catch (InvalidDomainEventException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new InvalidDomainEventException("Cannot parse domain event message", ex);
        }
    }

    private UUID resolveRecipientUserId(JsonNode root, JsonNode payload) {
        UUID recipient = firstUuid(root, payload, "recipient_user_id");
        if (recipient != null) {
            return recipient;
        }
        recipient = firstRecipientFromArray(root, payload);
        if (recipient != null) {
            return recipient;
        }
        recipient = firstUuid(root, payload, "post_author_id");
        if (recipient != null) {
            return recipient;
        }
        recipient = firstUuid(root, payload, "followed_user_id");
        if (recipient != null) {
            return recipient;
        }
        recipient = firstUuid(root, payload, "target_user_id");
        if (recipient != null) {
            return recipient;
        }
        recipient = firstUuid(root, payload, "buyer_id");
        if (recipient != null) {
            return recipient;
        }
        recipient = firstUuid(root, payload, "seller_user_id");
        if (recipient != null) {
            return recipient;
        }
        recipient = firstUuid(root, payload, "seller_id");
        if (recipient != null) {
            return recipient;
        }
        recipient = firstUuid(root, payload, "review_author_id");
        if (recipient != null) {
            return recipient;
        }
        recipient = firstUuid(root, payload, "shop_owner_id");
        if (recipient != null) {
            return recipient;
        }
        recipient = firstUuid(root, payload, "user_id");
        return recipient;
    }

    private UUID firstRecipientFromArray(JsonNode root, JsonNode payload) {
        JsonNode recipients = root.get("recipient_user_ids");
        if (recipients == null || recipients.isNull()) {
            recipients = payload.get("recipient_user_ids");
        }
        if (recipients != null && recipients.isArray() && !recipients.isEmpty()) {
            JsonNode first = recipients.get(0);
            if (first != null && first.isTextual()) {
                return uuidValue(first.asText(), "recipient_user_ids[0]");
            }
        }
        return null;
    }

    private UUID firstUuid(JsonNode primary, JsonNode secondary, String field) {
        UUID value = uuid(primary, field);
        return value != null ? value : uuid(secondary, field);
    }

    private String serializePayload(JsonNode payload) throws JsonProcessingException {
        if (payload == null || payload.isNull()) {
            return "{}";
        }
        return objectMapper.writeValueAsString(payload);
    }

    private String text(JsonNode node, String field) {
        if (node == null || !node.hasNonNull(field)) {
            return null;
        }
        String value = node.get(field).asText();
        return value.isBlank() ? null : value;
    }

    private UUID uuid(JsonNode node, String field) {
        String value = text(node, field);
        return value == null ? null : uuidValue(value, field);
    }

    private UUID uuidValue(String value, String field) {
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException ex) {
            throw new InvalidDomainEventException("Invalid UUID for field " + field);
        }
    }

    private Instant instant(JsonNode node, String field) {
        String value = text(node, field);
        if (value == null) {
            return null;
        }
        try {
            return Instant.parse(value);
        } catch (Exception ex) {
            throw new InvalidDomainEventException("Invalid instant for field " + field);
        }
    }
}
