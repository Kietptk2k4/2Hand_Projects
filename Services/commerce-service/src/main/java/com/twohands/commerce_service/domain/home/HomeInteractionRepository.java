package com.twohands.commerce_service.domain.home;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface HomeInteractionRepository {

    void saveImpressions(
            UUID userId,
            Instant shownAt,
            String requestId,
            RankingMode rankingMode,
            String modelName,
            Integer modelVersion,
            List<ServedCandidate> candidates
    );

    void saveClick(
            UUID userId,
            UUID productId,
            Instant occurredAt,
            String requestId
    );

    record ServedCandidate(CandidateProduct candidate, int rankPosition) {
    }
}
