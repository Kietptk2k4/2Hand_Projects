package com.twohands.social_service.domain.post;

import org.springframework.stereotype.Component;

@Component
public class RuleBasedRankingModel implements RankingModel {

    private static final double W_RECENCY = 0.15;
    private static final double W_ENGAGEMENT = 0.30;
    private static final double W_HASHTAG = 0.25;
    private static final double W_AUTHOR_AFFINITY = 0.15;
    private static final double W_MUTUAL_FOLLOW = 0.15;

    @Override
    public double predict(PostFeatureVector features) {
        if (features == null) {
            return 0.0;
        }
        return features.recencyScore() * W_RECENCY
                + features.engagementScore() * W_ENGAGEMENT
                + features.hashtagMatchScore() * W_HASHTAG
                + features.authorAffinityScore() * W_AUTHOR_AFFINITY
                + features.mutualFollowScore() * W_MUTUAL_FOLLOW;
    }
}
