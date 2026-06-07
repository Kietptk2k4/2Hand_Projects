package com.twohands.social_service.domain.trendinghashtag;

public record TrendingHashtag(
        String tag,
        long postCount,
        long totalLikes,
        long totalReplies,
        long engagementCount,
        long score
) {
}