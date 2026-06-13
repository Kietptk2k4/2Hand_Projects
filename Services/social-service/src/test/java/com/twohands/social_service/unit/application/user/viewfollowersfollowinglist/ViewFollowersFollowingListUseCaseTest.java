package com.twohands.social_service.unit.application.user.viewfollowersfollowinglist;

import com.twohands.social_service.application.user.viewfollowersfollowinglist.ViewFollowersFollowingListCommand;
import com.twohands.social_service.application.user.viewfollowersfollowinglist.ViewFollowersFollowingListResult;
import com.twohands.social_service.application.user.viewfollowersfollowinglist.ViewFollowersFollowingListUseCase;
import com.twohands.social_service.domain.follow.Follow;
import com.twohands.social_service.domain.follow.FollowRelationEntry;
import com.twohands.social_service.domain.follow.FollowRepository;
import com.twohands.social_service.domain.follow.FollowStatus;
import com.twohands.social_service.domain.follow.RelationListType;
import com.twohands.social_service.domain.post.PageResult;
import com.twohands.social_service.domain.user.UserProjection;
import com.twohands.social_service.domain.user.UserProjectionRepository;
import com.twohands.social_service.exception.AppException;
import com.twohands.social_service.exception.ErrorCode;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ViewFollowersFollowingListUseCaseTest {

    private final UserProjectionRepository userProjectionRepository = mock(UserProjectionRepository.class);
    private final FollowRepository followRepository = mock(FollowRepository.class);
    private final ViewFollowersFollowingListUseCase useCase = new ViewFollowersFollowingListUseCase(
            userProjectionRepository,
            followRepository
    );

    @Test
    void shouldReturnFollowersListForPublicAccount() {
        UUID viewerId = UUID.randomUUID();
        UUID targetId = UUID.randomUUID();
        UUID followerId = UUID.randomUUID();
        Instant followedAt = Instant.now();

        when(userProjectionRepository.findByUserId(targetId))
                .thenReturn(Optional.of(new UserProjection(targetId.toString(), "ACTIVE", "Target", null, null, false)));
        when(followRepository.findAcceptedFollowersPage(targetId, 0, 20))
                .thenReturn(new PageResult<>(List.of(new FollowRelationEntry(followerId, followedAt)), 0, 20, 1, 1, false));
        when(userProjectionRepository.findByUserId(followerId))
                .thenReturn(Optional.of(new UserProjection(followerId.toString(), "ACTIVE", "Follower", "https://avatar", null, false)));

        ViewFollowersFollowingListResult result = useCase.execute(
                new ViewFollowersFollowingListCommand(viewerId, targetId, RelationListType.FOLLOWERS, 0, 20)
        );

        assertThat(result.type()).isEqualTo(RelationListType.FOLLOWERS);
        assertThat(result.users().items()).hasSize(1);
        assertThat(result.users().items().getFirst().displayName()).isEqualTo("Follower");
    }

    @Test
    void shouldReturnFollowingList() {
        UUID viewerId = UUID.randomUUID();
        UUID targetId = UUID.randomUUID();
        UUID followeeId = UUID.randomUUID();

        when(userProjectionRepository.findByUserId(targetId))
                .thenReturn(Optional.of(new UserProjection(targetId.toString(), "ACTIVE", "Target", null, null, false)));
        when(followRepository.findAcceptedFollowingPage(targetId, 0, 20))
                .thenReturn(new PageResult<>(List.of(new FollowRelationEntry(followeeId, Instant.now())), 0, 20, 1, 1, false));
        when(userProjectionRepository.findByUserId(followeeId))
                .thenReturn(Optional.of(new UserProjection(followeeId.toString(), "ACTIVE", "Followee", null, null, false)));

        ViewFollowersFollowingListResult result = useCase.execute(
                new ViewFollowersFollowingListCommand(viewerId, targetId, RelationListType.FOLLOWING, 0, 20)
        );

        assertThat(result.type()).isEqualTo(RelationListType.FOLLOWING);
    }

    @Test
    void shouldThrowForbiddenForPrivateAccountWhenNotAcceptedFollower() {
        UUID viewerId = UUID.randomUUID();
        UUID targetId = UUID.randomUUID();

        when(userProjectionRepository.findByUserId(targetId))
                .thenReturn(Optional.of(new UserProjection(targetId.toString(), "ACTIVE", "Target", null, null, true)));
        when(followRepository.findByFollowerIdAndFolloweeId(viewerId, targetId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(
                new ViewFollowersFollowingListCommand(viewerId, targetId, RelationListType.FOLLOWERS, 0, 20)
        ))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode()).isEqualTo(ErrorCode.FORBIDDEN));
    }

    @Test
    void shouldAllowPrivateAccountListForAcceptedFollower() {
        UUID viewerId = UUID.randomUUID();
        UUID targetId = UUID.randomUUID();

        when(userProjectionRepository.findByUserId(targetId))
                .thenReturn(Optional.of(new UserProjection(targetId.toString(), "ACTIVE", "Target", null, null, true)));
        when(followRepository.findByFollowerIdAndFolloweeId(viewerId, targetId))
                .thenReturn(Optional.of(new Follow(viewerId, targetId, FollowStatus.ACCEPTED, Instant.now())));
        when(followRepository.findAcceptedFollowersPage(targetId, 0, 20))
                .thenReturn(new PageResult<>(List.of(), 0, 20, 0, 0, false));

        ViewFollowersFollowingListResult result = useCase.execute(
                new ViewFollowersFollowingListCommand(viewerId, targetId, RelationListType.FOLLOWERS, 0, 20)
        );

        assertThat(result.users().items()).isEmpty();
    }

    @Test
    void shouldThrowBadRequestForInvalidType() {
        assertThatThrownBy(() -> RelationListType.fromQuery("invalid"))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode()).isEqualTo(ErrorCode.BAD_REQUEST));
    }
}
