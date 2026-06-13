package com.twohands.social_service.application.user.viewsocialprofile;

import com.twohands.social_service.domain.follow.Follow;
import com.twohands.social_service.domain.follow.FollowRepository;
import com.twohands.social_service.domain.follow.FollowStatus;
import com.twohands.social_service.domain.user.UserProjection;
import com.twohands.social_service.domain.user.UserProjectionRepository;
import com.twohands.social_service.exception.AppException;
import com.twohands.social_service.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class ViewSocialProfileUseCase {

    private final UserProjectionRepository userProjectionRepository;
    private final FollowRepository followRepository;

    public ViewSocialProfileUseCase(
            UserProjectionRepository userProjectionRepository,
            FollowRepository followRepository
    ) {
        this.userProjectionRepository = userProjectionRepository;
        this.followRepository = followRepository;
    }

    @Transactional(readOnly = true)
    public ViewSocialProfileResult execute(ViewSocialProfileCommand command) {
        if (command.viewerId() == null) {
            throw new AppException(ErrorCode.UNAUTHORIZED, "Authentication required");
        }

        UserProjection target = userProjectionRepository.findByUserId(command.targetUserId())
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Nguoi dung khong ton tai."));

        if (target.isDeleted()) {
            throw new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Nguoi dung khong ton tai hoac da bi xoa.");
        }

        boolean isSelf = command.viewerId().equals(command.targetUserId());
        String followStatus = resolveFollowStatus(command.viewerId(), command.targetUserId(), isSelf);
        boolean canViewFullProfile = isSelf
                || !target.isPrivateProfile()
                || FollowStatus.ACCEPTED.name().equals(followStatus);

        Long followerCount = null;
        Long followingCount = null;
        if (canViewFullProfile) {
            followerCount = followRepository.countAcceptedFollowers(command.targetUserId());
            followingCount = followRepository.countAcceptedFollowing(command.targetUserId());
        }

        return new ViewSocialProfileResult(
                target.userId(),
                target.displayName(),
                target.avatarUrl(),
                target.coverUrl(),
                target.isPrivateProfile(),
                followerCount,
                followingCount,
                followStatus,
                canViewFullProfile
        );
    }

    public String successMessage() {
        return "Lay social profile thanh cong.";
    }

    private String resolveFollowStatus(UUID viewerId, UUID targetUserId, boolean isSelf) {
        if (isSelf) {
            return "SELF";
        }
        return followRepository.findByFollowerIdAndFolloweeId(viewerId, targetUserId)
                .map(Follow::status)
                .map(FollowStatus::name)
                .orElse("NONE");
    }
}
