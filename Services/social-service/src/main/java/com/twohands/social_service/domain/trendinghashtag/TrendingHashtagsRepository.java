package com.twohands.social_service.domain.trendinghashtag;

import java.time.Instant;
import java.util.List;

public interface TrendingHashtagsRepository {
    List<TrendingHashtag> findTrendingHashtags(Instant createdAfter, int limit, int postCountWeight);
}