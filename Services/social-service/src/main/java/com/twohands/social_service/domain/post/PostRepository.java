package com.twohands.social_service.domain.post;

import java.util.List;

public interface PostRepository {
    PageResult<Post> findGlobalFeed(FeedQuery query);

    PageResult<Post> findFollowingFeed(FeedQuery query, List<String> followeeIds);
}
