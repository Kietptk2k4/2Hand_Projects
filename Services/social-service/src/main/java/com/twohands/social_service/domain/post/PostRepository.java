package com.twohands.social_service.domain.post;

import java.util.List;
import java.util.Optional;

public interface PostRepository {
    Post save(Post post);

    Optional<Post> findById(String postId);

    void incrementReplyCount(String postId);

    PageResult<Post> findGlobalFeed(FeedQuery query);

    PageResult<Post> findFollowingFeed(FeedQuery query, List<String> followeeIds);
}
