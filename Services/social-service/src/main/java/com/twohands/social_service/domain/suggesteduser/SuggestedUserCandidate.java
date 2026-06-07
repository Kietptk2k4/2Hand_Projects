package com.twohands.social_service.domain.suggesteduser;

import java.util.UUID;

public record SuggestedUserCandidate(UUID userId, long mutualFollowCount) {
}