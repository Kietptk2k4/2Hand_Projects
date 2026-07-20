package com.twohands.social_service.domain.post;

public record PostFeatureVector(
        double recencyScore,
        double engagementScore,
        double hashtagMatchScore,
        double authorAffinityScore,
        double mutualFollowScore,
        double crossDomainProductScore
) {
}
