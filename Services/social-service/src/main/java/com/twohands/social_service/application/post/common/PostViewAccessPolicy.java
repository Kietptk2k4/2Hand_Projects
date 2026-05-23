package com.twohands.social_service.application.post.common;

import com.twohands.social_service.domain.post.Post;
import com.twohands.social_service.domain.post.PostStatus;
import com.twohands.social_service.domain.post.PostVisibility;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.UUID;

@Component
public class PostViewAccessPolicy {

    public boolean canView(Post post, UUID viewerId, Collection<String> acceptedFolloweeAuthorIds) {
        if (post == null || viewerId == null) {
            return false;
        }
        if (post.status() == PostStatus.DELETED) {
            return false;
        }
        if (post.status() == PostStatus.DRAFT) {
            return post.authorId().equals(viewerId.toString());
        }
        if (post.status() != PostStatus.ACTIVE) {
            return false;
        }
        if (post.visibility() == PostVisibility.PUBLIC) {
            return true;
        }
        if (post.visibility() == PostVisibility.FOLLOWERS) {
            return post.authorId().equals(viewerId.toString())
                    || (acceptedFolloweeAuthorIds != null && acceptedFolloweeAuthorIds.contains(post.authorId()));
        }
        return false;
    }
}
