package com.twohands.social_service.application.user.unfollowuser;

import com.twohands.social_service.domain.follow.FollowRepository;
import com.twohands.social_service.domain.user.UserProjectionRepository;
import com.twohands.social_service.exception.AppException;
import com.twohands.social_service.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UnfollowUserUseCase {

    private final FollowRepository followRepository;
    private final UserProjectionRepository userProjectionRepository;

    public UnfollowUserUseCase(
            FollowRepository followRepository,
            UserProjectionRepository userProjectionRepository
    ) {
        this.followRepository = followRepository;
        this.userProjectionRepository = userProjectionRepository;
    }

    @Transactional
    public UnfollowUserResult execute(UnfollowUserCommand command) {
        validateFollower(command);

        boolean wasFollowing = followRepository
                .findByFollowerIdAndFolloweeId(command.followerId(), command.followeeId())
                .isPresent();

        if (wasFollowing) {
            followRepository.deleteByFollowerIdAndFolloweeId(command.followerId(), command.followeeId());
        }

        return new UnfollowUserResult(command.followeeId(), wasFollowing);
    }

    public String successMessage() {
        return "Huy theo doi nguoi dung thanh cong.";
    }

    private void validateFollower(UnfollowUserCommand command) {
        if (command.followerId() == null) {
            throw new AppException(ErrorCode.UNAUTHORIZED, "Authentication required");
        }
        userProjectionRepository.findByUserId(command.followerId()).ifPresent(user -> {
            if (user.isActionForbidden()) {
                throw new AppException(ErrorCode.ACCOUNT_SUSPENDED,
                        ErrorCode.ACCOUNT_SUSPENDED.defaultMessage());
            }
        });
    }
}
