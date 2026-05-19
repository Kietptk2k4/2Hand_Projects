package com.twohands.social_service.application.user.followuser;

import com.twohands.social_service.application.user.common.UserFollowedOutboxService;
import com.twohands.social_service.domain.follow.Follow;
import com.twohands.social_service.domain.follow.FollowRepository;
import com.twohands.social_service.domain.follow.FollowStatus;
import com.twohands.social_service.domain.outbox.OutboxEventRepository;
import com.twohands.social_service.domain.user.UserProjection;
import com.twohands.social_service.domain.user.UserProjectionRepository;
import com.twohands.social_service.exception.AppException;
import com.twohands.social_service.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class FollowUserUseCase {

    private final FollowRepository followRepository;
    private final UserProjectionRepository userProjectionRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final UserFollowedOutboxService userFollowedOutboxService;

    public FollowUserUseCase(
            FollowRepository followRepository,
            UserProjectionRepository userProjectionRepository,
            OutboxEventRepository outboxEventRepository,
            UserFollowedOutboxService userFollowedOutboxService
    ) {
        this.followRepository = followRepository;
        this.userProjectionRepository = userProjectionRepository;
        this.outboxEventRepository = outboxEventRepository;
        this.userFollowedOutboxService = userFollowedOutboxService;
    }

    @Transactional
    public FollowUserResult execute(FollowUserCommand command) {
        validateFollower(command);

        if (command.followerId().equals(command.followeeId())) {
            throw new AppException(ErrorCode.BAD_REQUEST, "Khong the tu theo doi chinh minh.");
        }

        UserProjection followee = userProjectionRepository.findByUserId(command.followeeId())
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Nguoi dung khong ton tai."));

        if (followee.isDeleted()) {
            throw new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Nguoi dung khong ton tai hoac da bi xoa.");
        }

        return followRepository.findByFollowerIdAndFolloweeId(command.followerId(), command.followeeId())
                .map(existing -> new FollowUserResult(
                        existing.followeeId(),
                        existing.status(),
                        existing.createdAt(),
                        false
                ))
                .orElseGet(() -> createFollow(command, followee));
    }

    public String successMessage() {
        return "Theo doi nguoi dung thanh cong.";
    }

    private FollowUserResult createFollow(FollowUserCommand command, UserProjection followee) {
        FollowStatus status = followee.isPrivateProfile() ? FollowStatus.PENDING : FollowStatus.ACCEPTED;
        Instant now = Instant.now();
        Follow follow = new Follow(command.followerId(), command.followeeId(), status, now);

        followRepository.save(follow);
        outboxEventRepository.save(userFollowedOutboxService.build(
                command.followerId(),
                command.followeeId(),
                status,
                now
        ));

        return new FollowUserResult(command.followeeId(), status, now, true);
    }

    private void validateFollower(FollowUserCommand command) {
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
