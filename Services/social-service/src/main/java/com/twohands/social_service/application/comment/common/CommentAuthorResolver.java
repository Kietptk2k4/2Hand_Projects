package com.twohands.social_service.application.comment.common;

import com.twohands.social_service.domain.user.UserProjection;
import com.twohands.social_service.domain.user.UserProjectionRepository;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class CommentAuthorResolver {

    private static final String DELETED_ACCOUNT_DISPLAY_NAME = "Tai khoan da xoa";
    private static final String DEFAULT_DISPLAY_NAME = "User";

    private final UserProjectionRepository userProjectionRepository;

    public CommentAuthorResolver(UserProjectionRepository userProjectionRepository) {
        this.userProjectionRepository = userProjectionRepository;
    }

    public CommentAuthorSummary resolve(String authorId) {
        UUID authorUserId;
        try {
            authorUserId = UUID.fromString(authorId);
        } catch (IllegalArgumentException ex) {
            return new CommentAuthorSummary(authorId, DEFAULT_DISPLAY_NAME, null);
        }

        return userProjectionRepository.findByUserId(authorUserId)
                .map(this::toAuthorSummary)
                .orElseGet(() -> new CommentAuthorSummary(authorId, DEFAULT_DISPLAY_NAME, null));
    }

    private CommentAuthorSummary toAuthorSummary(UserProjection projection) {
        if (projection.isDeleted()) {
            return new CommentAuthorSummary(
                    projection.userId(),
                    DELETED_ACCOUNT_DISPLAY_NAME,
                    null
            );
        }
        return new CommentAuthorSummary(
                projection.userId(),
                projection.displayName(),
                projection.avatarUrl()
        );
    }
}