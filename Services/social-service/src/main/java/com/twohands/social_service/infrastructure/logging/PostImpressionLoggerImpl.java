package com.twohands.social_service.infrastructure.logging;

import com.twohands.social_service.application.feed.recommendposts.PostImpressionLogger;
import com.twohands.social_service.domain.post.PostImpressionRepository;
import com.twohands.social_service.domain.post.UserSeenPostsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Component
public class PostImpressionLoggerImpl implements PostImpressionLogger {

    private static final Logger log = LoggerFactory.getLogger(PostImpressionLoggerImpl.class);

    private final PostImpressionRepository postImpressionRepository;
    private final UserSeenPostsRepository userSeenPostsRepository;

    public PostImpressionLoggerImpl(
            PostImpressionRepository postImpressionRepository,
            UserSeenPostsRepository userSeenPostsRepository
    ) {
        this.postImpressionRepository = postImpressionRepository;
        this.userSeenPostsRepository = userSeenPostsRepository;
    }

    @Override
    public void logImpressions(
            UUID userId,
            List<String> postIds,
            List<Integer> rankPositions,
            Integer modelVersion,
            String modelName,
            String requestId
    ) {
        if (userId == null || postIds == null || postIds.isEmpty()) {
            return;
        }
        Instant now = Instant.now();
        List<PostImpressionRepository.ImpressionRow> rows = new ArrayList<>(postIds.size());
        for (int i = 0; i < postIds.size(); i++) {
            Integer rank = (rankPositions != null && i < rankPositions.size())
                    ? rankPositions.get(i)
                    : (i + 1);
            rows.add(new PostImpressionRepository.ImpressionRow(postIds.get(i), rank));
        }

        CompletableFuture.runAsync(() -> {
            try {
                postImpressionRepository.insertImpressions(
                        userId, rows, now, modelVersion, modelName, requestId
                );
                userSeenPostsRepository.upsertSeenPosts(userId, postIds, now);
            } catch (Exception ex) {
                log.error("Failed to persist impressions/seen posts for user {}", userId, ex);
            }
        });
    }
}
