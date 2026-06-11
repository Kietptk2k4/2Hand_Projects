package com.twohands.social_service.domain.post;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface PostRepository {
    Post save(Post post);

    Optional<Post> findById(String postId);

    List<Post> findByIds(Collection<String> postIds);

    void incrementReplyCount(String postId);

    void decrementReplyCount(String postId);

    void incrementLikeCount(String postId);

    void decrementLikeCount(String postId);

    PageResult<Post> findGlobalFeed(FeedQuery query);

    PageResult<Post> findFollowingFeed(FeedQuery query, List<String> followeeIds);

    PageResult<Post> searchPosts(PostSearchQuery query, List<String> acceptedFolloweeAuthorIds);

    PageResult<Post> searchPostsByHashtag(PostHashtagSearchQuery query, List<String> acceptedFolloweeAuthorIds);

    PageResult<Post> findAuthorPosts(AuthorPostsQuery query);

    long markProductTagsUnavailable(String productId);
}
