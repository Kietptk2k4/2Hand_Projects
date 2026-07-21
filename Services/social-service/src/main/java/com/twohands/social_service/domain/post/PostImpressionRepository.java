package com.twohands.social_service.domain.post;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface PostImpressionRepository {

    void insertImpressions(
            UUID userId,
            List<ImpressionRow> rows,
            Instant shownAt,
            Integer modelVersion,
            String modelName,
            String requestId
    );

    record ImpressionRow(String postId, Integer rankPosition) {
    }
}
