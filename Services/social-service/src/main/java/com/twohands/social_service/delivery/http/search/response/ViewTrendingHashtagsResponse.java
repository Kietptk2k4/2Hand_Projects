package com.twohands.social_service.delivery.http.search.response;

import java.util.List;

public record ViewTrendingHashtagsResponse(List<TrendingHashtagItemResponse> items) {

    public record TrendingHashtagItemResponse(
            String tag,
            long postCount,
            long totalLikes,
            long totalReplies,
            long engagementCount,
            long score
    ) {
    }
}