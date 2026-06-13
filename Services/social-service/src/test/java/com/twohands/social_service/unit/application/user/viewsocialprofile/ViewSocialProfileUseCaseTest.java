package com.twohands.social_service.unit.application.user.viewsocialprofile;

import com.twohands.social_service.application.user.viewsocialprofile.ViewSocialProfileCommand;
import com.twohands.social_service.application.user.viewsocialprofile.ViewSocialProfileResult;
import com.twohands.social_service.application.user.viewsocialprofile.ViewSocialProfileUseCase;
import com.twohands.social_service.domain.follow.Follow;
import com.twohands.social_service.domain.follow.FollowRepository;
import com.twohands.social_service.domain.follow.FollowStatus;
import com.twohands.social_service.domain.user.UserProjection;
import com.twohands.social_service.domain.user.UserProjectionRepository;
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

class ViewSocialProfileUseCaseTest {

    private final UserProjectionRepository userProjectionRepository = mock(UserProjectionRepository.class);
    private final FollowRepository followRepository = mock(FollowRepository.class);
    private final ViewSocialProfileUseCase useCase = new ViewSocialProfileUseCase(
            userProjectionRepository,
            followRepository
    );

    @Test
    void shouldReturnFullProfileForPublicAccount() {
        UUID viewerId = UUID.randomUUID();
        UUID targetId = UUID.randomUUID();

        when(userProjectionRepository.findByUserId(targetId))
                .thenReturn(Optional.of(new UserProjection(targetId.toString(), "ACTIVE", "User B", "https://avatar", "https://cover", false)));
        when(followRepository.findByFollowerIdAndFolloweeId(viewerId, targetId)).thenReturn(Optional.empty());
        when(followRepository.countAcceptedFollowers(targetId)).thenReturn(10L);
        when(followRepository.countAcceptedFollowing(targetId)).thenReturn(5L);

        ViewSocialProfileResult result = useCase.execute(new ViewSocialProfileCommand(viewerId, targetId));

        assertThat(result.canViewFullProfile()).isTrue();
        assertThat(result.followerCount()).isEqualTo(10L);
        assertThat(result.followingCount()).isEqualTo(5L);
        assertThat(result.followStatus()).isEqualTo("NONE");
        assertThat(result.isPrivate()).isFalse();
        assertThat(result.coverUrl()).isEqualTo("https://cover");
    }

    @Test
    void shouldMaskCountsForPrivateProfileWhenNotFollowing() {
        UUID viewerId = UUID.randomUUID();
        UUID targetId = UUID.randomUUID();

        when(userProjectionRepository.findByUserId(targetId))
                .thenReturn(Optional.of(new UserProjection(targetId.toString(), "ACTIVE", "User B", "https://avatar", null, true)));
        when(followRepository.findByFollowerIdAndFolloweeId(viewerId, targetId)).thenReturn(Optional.empty());

        ViewSocialProfileResult result = useCase.execute(new ViewSocialProfileCommand(viewerId, targetId));

        assertThat(result.canViewFullProfile()).isFalse();
        assertThat(result.followerCount()).isNull();
        assertThat(result.followingCount()).isNull();
        assertThat(result.isPrivate()).isTrue();
        verify(followRepository, never()).countAcceptedFollowers(targetId);
    }

    @Test
    void shouldReturnFullProfileForPrivateAccountWhenAcceptedFollower() {
        UUID viewerId = UUID.randomUUID();
        UUID targetId = UUID.randomUUID();

        when(userProjectionRepository.findByUserId(targetId))
                .thenReturn(Optional.of(new UserProjection(targetId.toString(), "ACTIVE", "User B", "https://avatar", null, true)));
        when(followRepository.findByFollowerIdAndFolloweeId(viewerId, targetId))
                .thenReturn(Optional.of(new Follow(viewerId, targetId, FollowStatus.ACCEPTED, Instant.now())));
        when(followRepository.countAcceptedFollowers(targetId)).thenReturn(3L);
        when(followRepository.countAcceptedFollowing(targetId)).thenReturn(1L);

        ViewSocialProfileResult result = useCase.execute(new ViewSocialProfileCommand(viewerId, targetId));

        assertThat(result.canViewFullProfile()).isTrue();
        assertThat(result.followStatus()).isEqualTo("ACCEPTED");
        assertThat(result.followerCount()).isEqualTo(3L);
    }

    @Test
    void shouldReturnSelfProfileWithCounts() {
        UUID userId = UUID.randomUUID();

        when(userProjectionRepository.findByUserId(userId))
                .thenReturn(Optional.of(new UserProjection(userId.toString(), "ACTIVE", "Me", "https://avatar", null, true)));
        when(followRepository.countAcceptedFollowers(userId)).thenReturn(2L);
        when(followRepository.countAcceptedFollowing(userId)).thenReturn(4L);

        ViewSocialProfileResult result = useCase.execute(new ViewSocialProfileCommand(userId, userId));

        assertThat(result.followStatus()).isEqualTo("SELF");
        assertThat(result.canViewFullProfile()).isTrue();
        verify(followRepository, never()).findByFollowerIdAndFolloweeId(userId, userId);
    }

    @Test
    void shouldThrowNotFoundWhenUserDoesNotExist() {
        UUID viewerId = UUID.randomUUID();
        UUID targetId = UUID.randomUUID();
        when(userProjectionRepository.findByUserId(targetId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(new ViewSocialProfileCommand(viewerId, targetId)))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode()).isEqualTo(ErrorCode.RESOURCE_NOT_FOUND));
    }

    @Test
    void shouldThrowNotFoundWhenUserIsDeleted() {
        UUID viewerId = UUID.randomUUID();
        UUID targetId = UUID.randomUUID();
        when(userProjectionRepository.findByUserId(targetId))
                .thenReturn(Optional.of(new UserProjection(targetId.toString(), "DELETED", "User", null, null, false)));

        assertThatThrownBy(() -> useCase.execute(new ViewSocialProfileCommand(viewerId, targetId)))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode()).isEqualTo(ErrorCode.RESOURCE_NOT_FOUND));
    }
}
