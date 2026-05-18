package com.twohands.social_service.domain.post;

public interface PostRepository {
    PageResult<Post> findGlobalFeed(FeedQuery query);
}
