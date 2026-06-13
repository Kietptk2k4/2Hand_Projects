package com.twohands.social_service.unit.application.user.unfollowuser;

import com.twohands.social_service.application.user.unfollowuser.UnfollowUserCommand;
import com.twohands.social_service.application.user.unfollowuser.UnfollowUserResult;
import com.twohands.social_service.application.user.unfollowuser.UnfollowUserUseCase;
import com.twohands.social_service.domain.follow.Follow;
import com.twohands.social_service.domain.follow.FollowRepository;
import com.twohands.social_service.domain.follow.FollowStatus;
import com.twohands.social_service.domain.user.UserProjection;
import com.twohands.social_service.application.user.common.UserWriteGuard;
import com.twohands.social_service.domain.user.UserProjectionRepository;
import com.twohands.social_service.testsupport.UserProjectionTestFixtures;
import com.twohands.social_service.exception.AppException;
import com.twohands.social_service.exception.ErrorCode;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UnfollowUserUseCaseTest {

    private final FollowRepository followRepository = mock(FollowRepository.class);
    private final UserProjectionRepository userProjectionRepository = mock(UserProjectionRepository.class);
    private final UserWriteGuard userWriteGuard = new UserWriteGuard(userProjectionRepository);
    private final UnfollowUserUseCase useCase = new UnfollowUserUseCase(followRepository, userWriteGuard);

    @Test
    void shouldDeleteFollowRelationWhenFollowing() {
        UUID followerId = UUID.randomUUID();
        UUID followeeId = UUID.randomUUID();
        Follow existing = new Follow(followerId, followeeId, FollowStatus.ACCEPTED, Instant.now());

        when(userProjectionRepository.findByUserId(followerId)).thenReturn(UserProjectionTestFixtures.activeOptional(followerId));
        when(followRepository.findByFollowerIdAndFolloweeId(followerId, followeeId)).thenReturn(Optional.of(existing));

        UnfollowUserResult result = useCase.execute(new UnfollowUserCommand(followerId, followeeId));

        verify(followRepository).deleteByFollowerIdAndFolloweeId(followerId, followeeId);
        assertThat(result.wasFollowing()).isTrue();
        assertThat(result.followeeId()).isEqualTo(followeeId);
    }

    @Test
    void shouldSucceedIdempotentlyWhenNotFollowing() {
        UUID followerId = UUID.randomUUID();
        UUID followeeId = UUID.randomUUID();

        when(userProjectionRepository.findByUserId(followerId)).thenReturn(UserProjectionTestFixtures.activeOptional(followerId));
        when(followRepository.findByFollowerIdAndFolloweeId(followerId, followeeId)).thenReturn(Optional.empty());

        UnfollowUserResult result = useCase.execute(new UnfollowUserCommand(followerId, followeeId));

        verify(followRepository, never()).deleteByFollowerIdAndFolloweeId(followerId, followeeId);
        assertThat(result.wasFollowing()).isFalse();
    }

    @Test
    void shouldThrowForbiddenWhenFollowerIsSuspended() {
        UUID followerId = UUID.randomUUID();
        when(userProjectionRepository.findByUserId(followerId))
                .thenReturn(Optional.of(new UserProjection(followerId.toString(), "SUSPENDED", "User", null, null, false)));

        assertThatThrownBy(() -> useCase.execute(new UnfollowUserCommand(followerId, UUID.randomUUID())))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode()).isEqualTo(ErrorCode.ACCOUNT_SUSPENDED));
    }
}
