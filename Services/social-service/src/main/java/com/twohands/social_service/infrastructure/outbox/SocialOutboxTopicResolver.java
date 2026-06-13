package com.twohands.social_service.infrastructure.outbox;

import com.twohands.social_service.exception.AppException;
import com.twohands.social_service.exception.ErrorCode;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class SocialOutboxTopicResolver {

    private static final Map<String, String> EVENT_TYPE_TO_TOPIC = Map.of(
            "POST_CREATED", "social.post.created",
            "POST_LIKED", "social.post.liked",
            "COMMENT_LIKED", "social.comment.liked",
            "COMMENT_CREATED", "social.comment.created",
            "USER_FOLLOWED", "social.user.followed",
            "USER_AVATAR_UPDATED", "social.user.avatar_updated"
    );

    public String resolve(String eventType) {
        String topic = EVENT_TYPE_TO_TOPIC.get(eventType);
        if (topic == null) {
            throw new AppException(
                    ErrorCode.INTERNAL_ERROR,
                    "Unsupported outbox event type for publish: " + eventType
            );
        }
        return topic;
    }
}
