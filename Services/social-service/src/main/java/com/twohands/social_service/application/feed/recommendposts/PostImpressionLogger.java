package com.twohands.social_service.application.feed.recommendposts;

import java.util.List;
import java.util.UUID;

public interface PostImpressionLogger {

    void logImpressions(
            UUID userId,
            List<String> postIds,
            List<Integer> rankPositions,
            Integer modelVersion,
            String modelName,
            String requestId
    );
}
