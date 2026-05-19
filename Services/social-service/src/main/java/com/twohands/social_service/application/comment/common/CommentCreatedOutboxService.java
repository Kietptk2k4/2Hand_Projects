package com.twohands.social_service.application.comment.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.twohands.social_service.domain.comment.Comment;
import com.twohands.social_service.domain.outbox.OutboxEvent;
import com.twohands.social_service.domain.outbox.OutboxStatus;
import com.twohands.social_service.exception.AppException;
import com.twohands.social_service.exception.ErrorCode;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
public class CommentCreatedOutboxService {

    private static final String COMMENT_CREATED_EVENT_TYPE = "COMMENT_CREATED";

    private final ObjectMapper objectMapper;

    public CommentCreatedOutboxService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public OutboxEvent build(Comment comment, Instant now) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("comment_id", comment.id());
        payload.put("post_id", comment.postId());
        payload.put("author_id", comment.authorId());
        if (comment.parentCommentId() != null) {
            payload.put("parent_comment_id", comment.parentCommentId());
        }

        String payloadJson;
        try {
            payloadJson = objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException ex) {
            throw new AppException(ErrorCode.INTERNAL_ERROR, "Cannot serialize outbox payload", ex);
        }

        return new OutboxEvent(
                UUID.randomUUID(),
                COMMENT_CREATED_EVENT_TYPE,
                comment.id(),
                payloadJson,
                OutboxStatus.PENDING,
                0,
                now,
                null
        );
    }
}
