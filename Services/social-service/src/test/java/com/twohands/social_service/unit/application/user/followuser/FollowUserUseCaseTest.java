package com.twohands.social_service.unit.application.user.followuser;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.twohands.social_service.application.user.common.UserFollowedOutboxService;
import com.twohands.social_service.application.user.common.UserWriteGuard;
import com.twohands.social_service.testsupport.UserProjectionTestFixtures;
import com.twohands.social_service.application.user.followuser.FollowUserCommand;
import com.twohands.social_service.application.user.followuser.FollowUserResult;
import com.twohands.social_service.application.user.followuser.FollowUserUseCase;
import com.twohands.social_service.domain.follow.Follow;
import com.twohands.social_service.domain.follow.FollowRepository;
import com.twohands.social_service.domain.follow.FollowStatus;
import com.twohands.social_service.domain.outbox.OutboxEvent;
import com.twohands.social_service.domain.outbox.OutboxEventRepository;
import com.twohands.social_service.domain.outbox.OutboxStatus;
import com.twohands.social_service.domain.user.UserProjection;
import com.twohands.social_service.domain.user.UserProjectionRepository;
import com.twohands.social_service.exception.AppException;
import com.twohands.social_service.exception.ErrorCode;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FollowUserUseCaseTest {

    private final FollowRepository followRepository = mock(FollowRepository.class);
    private final UserProjectionRepository userProjectionRepository = mock(UserProjectionRepository.class);
    private final UserWriteGuard userWriteGuard = new UserWriteGuard(userProjectionRepository);
    private final OutboxEventRepository outboxEventRepository = mock(OutboxEventRepository.class);
    private final UserFollowedOutboxService userFollowedOutboxService = new UserFollowedOutboxService(new ObjectMapper());
    private final FollowUserUseCase useCase = new FollowUserUseCase(
            followRepository,
            userProjectionRepository,
            userWriteGuard,
            outboxEventRepository,
            userFollowedOutboxService
    );

    @Test
    void shouldCreateAcceptedFollowForPublicAccount() {
        UUID followerId = UUID.randomUUID();
        UUID followeeId = UUID.randomUUID();
        Instant now = Instant.now();

        when(userProjectionRepository.findByUserId(followerId)).thenReturn(UserProjectionTestFixtures.activeOptional(followerId));
        when(userProjectionRepository.findByUserId(followeeId))
                .thenReturn(Optional.of(new UserProjection(followeeId.toString(), "ACTIVE", "User B", null, null, false)));
        when(followRepository.findByFollowerIdAndFolloweeId(followerId, followeeId)).thenReturn(Optional.empty());
        when(outboxEventRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        FollowUserResult result = useCase.execute(new FollowUserCommand(followerId, followeeId));

        ArgumentCaptor<Follow> followCaptor = ArgumentCaptor.forClass(Follow.class);
        verify(followRepository).save(followCaptor.capture());
        assertThat(followCaptor.getValue().status()).isEqualTo(FollowStatus.ACCEPTED);

        ArgumentCaptor<OutboxEvent> outboxCaptor = ArgumentCaptor.forClass(OutboxEvent.class);
        verify(outboxEventRepository).save(outboxCaptor.capture());
        assertThat(outboxCaptor.getValue().eventType()).isEqualTo("USER_FOLLOWED");
        assertThat(outboxCaptor.getValue().status()).isEqualTo(OutboxStatus.PENDING);

        assertThat(result.newlyCreated()).isTrue();
        assertThat(result.status()).isEqualTo(FollowStatus.ACCEPTED);
    }

    @Test
    void shouldCreatePendingFollowForPrivateAccount() {
        UUID followerId = UUID.randomUUID();
        UUID followeeId = UUID.randomUUID();

        when(userProjectionRepository.findByUserId(followerId)).thenReturn(UserProjectionTestFixtures.activeOptional(followerId));
        when(userProjectionRepository.findByUserId(followeeId))
                .thenReturn(Optional.of(new UserProjection(followeeId.toString(), "ACTIVE", "User B", null, null, true)));
        when(followRepository.findByFollowerIdAndFolloweeId(followerId, followeeId)).thenReturn(Optional.empty());
        when(outboxEventRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        FollowUserResult result = useCase.execute(new FollowUserCommand(followerId, followeeId));

        ArgumentCaptor<Follow> followCaptor = ArgumentCaptor.forClass(Follow.class);
        verify(followRepository).save(followCaptor.capture());
        assertThat(followCaptor.getValue().status()).isEqualTo(FollowStatus.PENDING);
        assertThat(result.status()).isEqualTo(FollowStatus.PENDING);
    }

    @Test
    void shouldReturnExistingFollowWithoutOutboxWhenAlreadyFollowing() {
        UUID followerId = UUID.randomUUID();
        UUID followeeId = UUID.randomUUID();
        Instant createdAt = Instant.now();
        Follow existing = new Follow(followerId, followeeId, FollowStatus.ACCEPTED, createdAt);

        when(userProjectionRepository.findByUserId(followerId)).thenReturn(UserProjectionTestFixtures.activeOptional(followerId));
        when(userProjectionRepository.findByUserId(followeeId))
                .thenReturn(Optional.of(new UserProjection(followeeId.toString(), "ACTIVE", "User B", null, null, false)));
        when(followRepository.findByFollowerIdAndFolloweeId(followerId, followeeId)).thenReturn(Optional.of(existing));

        FollowUserResult result = useCase.execute(new FollowUserCommand(followerId, followeeId));

        verify(followRepository, never()).save(any());
        verify(outboxEventRepository, never()).save(any());
        assertThat(result.newlyCreated()).isFalse();
        assertThat(result.createdAt()).isEqualTo(createdAt);
    }

    @Test
    void shouldThrowBadRequestWhenSelfFollow() {
        UUID userId = UUID.randomUUID();
        when(userProjectionRepository.findByUserId(userId)).thenReturn(UserProjectionTestFixtures.activeOptional(userId));

        assertThatThrownBy(() -> useCase.execute(new FollowUserCommand(userId, userId)))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode()).isEqualTo(ErrorCode.BAD_REQUEST));
    }

    @Test
    void shouldThrowNotFoundWhenFolloweeDoesNotExist() {
        UUID followerId = UUID.randomUUID();
        UUID followeeId = UUID.randomUUID();

        when(userProjectionRepository.findByUserId(followerId)).thenReturn(UserProjectionTestFixtures.activeOptional(followerId));
        when(userProjectionRepository.findByUserId(followeeId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(new FollowUserCommand(followerId, followeeId)))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode()).isEqualTo(ErrorCode.RESOURCE_NOT_FOUND));
    }

    @Test
    void shouldThrowForbiddenWhenFollowerIsSuspended() {
        UUID followerId = UUID.randomUUID();
        when(userProjectionRepository.findByUserId(followerId))
                .thenReturn(Optional.of(new UserProjection(followerId.toString(), "SUSPENDED", "User", null, null, false)));

        assertThatThrownBy(() -> useCase.execute(new FollowUserCommand(followerId, UUID.randomUUID())))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode()).isEqualTo(ErrorCode.ACCOUNT_SUSPENDED));
    }
}
