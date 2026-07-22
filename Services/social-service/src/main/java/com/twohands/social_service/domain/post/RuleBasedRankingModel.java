package com.twohands.social_service.domain.post;

import org.springframework.stereotype.Component;

@Component
public class RuleBasedRankingModel implements RankingModel {

    /** Locked Phase-1 weights (sum = 1.0); must match offline evaluate baseline. */
    public static final double W_RECENCY = 0.12;
    public static final double W_ENGAGEMENT = 0.28;
    public static final double W_HASHTAG = 0.22;
    public static final double W_AUTHOR_AFFINITY = 0.13;
    public static final double W_MUTUAL_FOLLOW = 0.13;
    public static final double W_CROSS_DOMAIN = 0.12;

    @Override
    public double predict(PostFeatureVector features) {
        if (features == null) {
            return 0.0;
        }
        return features.recencyScore() * W_RECENCY
                + features.engagementScore() * W_ENGAGEMENT
                + features.hashtagMatchScore() * W_HASHTAG
                + features.authorAffinityScore() * W_AUTHOR_AFFINITY
                + features.mutualFollowScore() * W_MUTUAL_FOLLOW
                + features.crossDomainProductScore() * W_CROSS_DOMAIN;
    }
}
