package com.twohands.social_service.application.reaction.common;

import com.twohands.social_service.domain.user.UserProjection;
import com.twohands.social_service.domain.user.UserProjectionRepository;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
public class LikeUserEnricher {

    private static final String DELETED_ACCOUNT_DISPLAY_NAME = "Tai khoan da xoa";
    private static final String DEFAULT_DISPLAY_NAME = "User";

    private final UserProjectionRepository userProjectionRepository;

    public LikeUserEnricher(UserProjectionRepository userProjectionRepository) {
        this.userProjectionRepository = userProjectionRepository;
    }

    public ViewLikeUsersResult.LikeUserItem enrich(UUID userId, Instant likedAt) {
        return userProjectionRepository.findByUserId(userId)
                .map(projection -> toItem(projection, likedAt))
                .orElseGet(() -> new ViewLikeUsersResult.LikeUserItem(
                        userId.toString(),
                        DEFAULT_DISPLAY_NAME,
                        null,
                        likedAt != null ? likedAt.toString() : null
                ));
    }

    private ViewLikeUsersResult.LikeUserItem toItem(UserProjection projection, Instant likedAt) {
        if (projection.isDeleted()) {
            return new ViewLikeUsersResult.LikeUserItem(
                    projection.userId(),
                    DELETED_ACCOUNT_DISPLAY_NAME,
                    null,
                    likedAt != null ? likedAt.toString() : null
            );
        }
        return new ViewLikeUsersResult.LikeUserItem(
                projection.userId(),
                projection.displayName(),
                projection.avatarUrl(),
                likedAt != null ? likedAt.toString() : null
        );
    }
}