package com.twohands.social_service.unit.application.user.viewsuggestedusers;

import com.twohands.social_service.application.user.viewsuggestedusers.ViewSuggestedUsersCommand;
import com.twohands.social_service.application.user.viewsuggestedusers.ViewSuggestedUsersResult;
import com.twohands.social_service.application.user.viewsuggestedusers.ViewSuggestedUsersUseCase;
import com.twohands.social_service.domain.follow.FollowRepository;
import com.twohands.social_service.domain.suggesteduser.SuggestedUsersRepository;
import com.twohands.social_service.domain.user.UserProjection;
import com.twohands.social_service.domain.user.UserProjectionRepository;
import com.twohands.social_service.exception.AppException;
import com.twohands.social_service.exception.ErrorCode;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ViewSuggestedUsersUseCaseTest {

    private final FollowRepository followRepository = mock(FollowRepository.class);
    private final UserProjectionRepository userProjectionRepository = mock(UserProjectionRepository.class);
    private final SuggestedUsersRepository suggestedUsersRepository = mock(SuggestedUsersRepository.class);
    private final ViewSuggestedUsersUseCase useCase = new ViewSuggestedUsersUseCase(
            followRepository,
            userProjectionRepository,
            suggestedUsersRepository
    );

    @Test
    void shouldReturnSuggestedUsersSortedByMutualFollowCount() {
        UUID viewerId = UUID.randomUUID();
        UUID highMutualId = UUID.randomUUID();
        UUID lowMutualId = UUID.randomUUID();

        when(followRepository.findFolloweeIdsByFollowerId(viewerId)).thenReturn(List.of());
        when(userProjectionRepository.findActiveSuggestionCandidatesExcluding(any(), anyInt()))
                .thenReturn(List.of(
                        new UserProjection(lowMutualId.toString(), "ACTIVE", "Beta User", null, null, false),
                        new UserProjection(highMutualId.toString(), "ACTIVE", "Alpha User", "https://avatar", null, false)
                ));
        when(suggestedUsersRepository.findMutualFollowCounts(eq(viewerId), any()))
                .thenReturn(Map.of(highMutualId, 3L, lowMutualId, 1L));

        ViewSuggestedUsersResult result = useCase.execute(new ViewSuggestedUsersCommand(viewerId, 0, 3));

        assertThat(result.users().items()).hasSize(2);
        assertThat(result.users().items().getFirst().userId()).isEqualTo(highMutualId.toString());
        assertThat(result.users().items().getFirst().displayName()).isEqualTo("Alpha User");
        assertThat(result.users().items().getFirst().mutualFollowCount()).isEqualTo(3L);
        assertThat(result.users().items().get(1).userId()).isEqualTo(lowMutualId.toString());
    }

    @Test
    void shouldExcludeViewerAndFollowedUsersFromCandidates() {
        UUID viewerId = UUID.randomUUID();
        UUID followedId = UUID.randomUUID();

        when(followRepository.findFolloweeIdsByFollowerId(viewerId)).thenReturn(List.of(followedId));
        when(userProjectionRepository.findActiveSuggestionCandidatesExcluding(
                eq(List.of(viewerId.toString(), followedId.toString())),
                anyInt()
        )).thenReturn(List.of());

        ViewSuggestedUsersResult result = useCase.execute(new ViewSuggestedUsersCommand(viewerId, 0, 3));

        assertThat(result.users().items()).isEmpty();
    }

    @Test
    void shouldRequireAuthentication() {
        assertThatThrownBy(() -> useCase.execute(new ViewSuggestedUsersCommand(null, 0, 3)))
                .isInstanceOf(AppException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.UNAUTHORIZED);
    }
}