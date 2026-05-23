package com.twohands.social_service.unit.application.search.searchhashtag;

import com.twohands.social_service.application.search.searchhashtag.SearchHashtagCommand;
import com.twohands.social_service.application.search.searchhashtag.SearchHashtagResult;
import com.twohands.social_service.application.search.searchhashtag.SearchHashtagUseCase;
import com.twohands.social_service.domain.follow.FollowRepository;
import com.twohands.social_service.domain.post.MediaItem;
import com.twohands.social_service.domain.post.PageResult;
import com.twohands.social_service.domain.post.Post;
import com.twohands.social_service.domain.post.PostModerationStatus;
import com.twohands.social_service.domain.post.PostHashtagSearchQuery;
import com.twohands.social_service.domain.post.PostRepository;
import com.twohands.social_service.domain.post.PostStatus;
import com.twohands.social_service.domain.post.PostVisibility;
import com.twohands.social_service.exception.AppException;
import com.twohands.social_service.exception.ErrorCode;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SearchHashtagUseCaseTest {

    private final PostRepository postRepository = mock(PostRepository.class);
    private final FollowRepository followRepository = mock(FollowRepository.class);
    private final SearchHashtagUseCase useCase = new SearchHashtagUseCase(postRepository, followRepository);

    private Post buildPost() {
        return new Post(
                "507f1f77bcf86cd799439011",
                UUID.randomUUID().toString(),
                "caption",
                List.of(new MediaItem("https://cdn/1.jpg", "IMAGE")),
                List.of(),
                PostStatus.ACTIVE,
                PostVisibility.PUBLIC,
                1L,
                0L,
                List.of("travel"),
                true,
                PostModerationStatus.NONE,
                null,
                null,
                Instant.now(),
                Instant.now(),
                null
        );
    }

    @Test
    void shouldSearchByHashtagWithoutHashPrefix() {
        UUID userId = UUID.randomUUID();
        when(followRepository.findAcceptedFolloweeIds(userId)).thenReturn(List.of());
        when(postRepository.searchPostsByHashtag(any(PostHashtagSearchQuery.class), any()))
                .thenReturn(new PageResult<>(List.of(buildPost()), 0, 20, 1, 1, false));

        SearchHashtagResult result = useCase.execute(new SearchHashtagCommand(userId, "#travel", 0, 20));

        ArgumentCaptor<PostHashtagSearchQuery> queryCaptor = ArgumentCaptor.forClass(PostHashtagSearchQuery.class);
        verify(postRepository).searchPostsByHashtag(queryCaptor.capture(), any());
        assertThat(queryCaptor.getValue().hashtagVariants()).contains("travel", "#travel");
        assertThat(result.hashtag()).isEqualTo("travel");
        assertThat(result.items()).hasSize(1);
    }

    @Test
    void shouldThrowBadRequestWhenHashtagIsBlank() {
        UUID userId = UUID.randomUUID();

        assertThatThrownBy(() -> useCase.execute(new SearchHashtagCommand(userId, "  ", 0, 20)))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode()).isEqualTo(ErrorCode.BAD_REQUEST));
    }

    @Test
    void shouldThrowBadRequestWhenHashtagHasInvalidCharacters() {
        UUID userId = UUID.randomUUID();

        assertThatThrownBy(() -> useCase.execute(new SearchHashtagCommand(userId, "bad tag!", 0, 20)))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode()).isEqualTo(ErrorCode.BAD_REQUEST));
    }
}
