package com.twohands.social_service.application.user.unfollowuser;

import com.twohands.social_service.domain.follow.FollowRepository;
import com.twohands.social_service.application.user.common.UserWriteGuard;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UnfollowUserUseCase {

    private final FollowRepository followRepository;
    private final UserWriteGuard userWriteGuard;

    public UnfollowUserUseCase(
            FollowRepository followRepository,
            UserWriteGuard userWriteGuard
    ) {
        this.followRepository = followRepository;
        this.userWriteGuard = userWriteGuard;
    }

    @Transactional
    public UnfollowUserResult execute(UnfollowUserCommand command) {
        userWriteGuard.assertCanWrite(command.followerId());

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

}
