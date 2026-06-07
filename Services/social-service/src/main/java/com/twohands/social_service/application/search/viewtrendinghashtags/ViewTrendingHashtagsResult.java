package com.twohands.social_service.application.search.viewtrendinghashtags;

import java.util.List;

public record ViewTrendingHashtagsResult(List<TrendingHashtagItem> items) {

    public record TrendingHashtagItem(
            String tag,
            long postCount,
            long totalLikes,
            long totalReplies,
            long engagementCount,
            long score
    ) {
    }
}