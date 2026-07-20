package com.twohands.social_service.infrastructure.logging;

import com.twohands.social_service.application.feed.recommendposts.PostImpressionLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Component
public class PostImpressionLoggerImpl implements PostImpressionLogger {

    private static final Logger log = LoggerFactory.getLogger(PostImpressionLoggerImpl.class);

    @Override
    public void logImpressions(UUID userId, List<String> postIds) {
        if (postIds == null || postIds.isEmpty()) {
            return;
        }
        CompletableFuture.runAsync(() -> {
            log.info("Logging impressions asynchronously for user: {}, posts: {}", userId, postIds);
            // TODO: Insert into post_impression_log table when database migration is ready (Nhiệm vụ 3)
        });
    }
}
