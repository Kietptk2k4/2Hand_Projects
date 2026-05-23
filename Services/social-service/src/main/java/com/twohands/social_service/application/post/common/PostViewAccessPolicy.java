package com.twohands.social_service.application.post.common;

import com.twohands.social_service.domain.post.Post;
import com.twohands.social_service.domain.post.PostModerationStatus;
import com.twohands.social_service.domain.post.PostStatus;
import com.twohands.social_service.domain.post.PostVisibility;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.UUID;

@Component
public class PostViewAccessPolicy {

    public enum PostViewAccessOutcome {
        ALLOWED,
        NOT_FOUND,
        FORBIDDEN
    }

    public PostViewAccessOutcome evaluateViewAccess(
            Post post,
            UUID viewerId,
            Collection<String> acceptedFolloweeAuthorIds
    ) {
        if (viewerId == null) {
            return PostViewAccessOutcome.NOT_FOUND;
        }
        if (post == null) {
            return PostViewAccessOutcome.NOT_FOUND;
        }
        if (post.status() == PostStatus.DELETED) {
            return PostViewAccessOutcome.NOT_FOUND;
        }
        if (post.moderationStatusOrDefault() == PostModerationStatus.HIDDEN
                && !post.authorId().equals(viewerId.toString())) {
            return PostViewAccessOutcome.NOT_FOUND;
        }
        if (post.status() == PostStatus.DRAFT) {
            if (post.authorId().equals(viewerId.toString())) {
                return PostViewAccessOutcome.ALLOWED;
            }
            return PostViewAccessOutcome.NOT_FOUND;
        }
        if (post.status() != PostStatus.ACTIVE) {
            return PostViewAccessOutcome.NOT_FOUND;
        }
        if (post.visibility() == PostVisibility.PUBLIC) {
            return PostViewAccessOutcome.ALLOWED;
        }
        if (post.visibility() == PostVisibility.FOLLOWERS) {
            if (post.authorId().equals(viewerId.toString())) {
                return PostViewAccessOutcome.ALLOWED;
            }
            if (acceptedFolloweeAuthorIds != null && acceptedFolloweeAuthorIds.contains(post.authorId())) {
                return PostViewAccessOutcome.ALLOWED;
            }
            return PostViewAccessOutcome.FORBIDDEN;
        }
        return PostViewAccessOutcome.NOT_FOUND;
    }

    public boolean canView(Post post, UUID viewerId, Collection<String> acceptedFolloweeAuthorIds) {
        return evaluateViewAccess(post, viewerId, acceptedFolloweeAuthorIds) == PostViewAccessOutcome.ALLOWED;
    }
}
