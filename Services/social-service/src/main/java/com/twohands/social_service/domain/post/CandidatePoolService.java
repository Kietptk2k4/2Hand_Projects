package com.twohands.social_service.domain.post;

import java.util.List;
import java.util.UUID;

public interface CandidatePoolService {
    List<PostCandidate> getCandidates(UUID userId, int maxSize);
}
