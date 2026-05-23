package com.twohands.social_service.unit.application.feed.viewglobalfeed;

import com.twohands.social_service.application.feed.viewglobalfeed.ViewGlobalFeedResult;
import com.twohands.social_service.application.feed.viewglobalfeed.ViewGlobalFeedUseCase;
import com.twohands.social_service.domain.post.FeedQuery;
import com.twohands.social_service.domain.post.MediaItem;
import com.twohands.social_service.domain.post.PageResult;
import com.twohands.social_service.domain.post.Post;
import com.twohands.social_service.domain.post.PostModerationStatus;
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

class ViewGlobalFeedUseCaseTest {

    private final PostRepository postRepository = mock(PostRepository.class);
    private final ViewGlobalFeedUseCase useCase = new ViewGlobalFeedUseCase(postRepository);

    @Test
    void shouldReturnGlobalFeedWithMappedItems() {
        UUID userId = UUID.randomUUID();
        Post post = new Post(
                "507f1f77bcf86cd799439011",
                UUID.randomUUID().toString(),
                "caption",
                List.of(new MediaItem("https://cdn/1.jpg", "IMAGE")),
                List.of(),
                PostStatus.ACTIVE,
                PostVisibility.PUBLIC,
                10,
                2,
                List.of("java", "spring"),
                true,
                PostModerationStatus.NONE,
                null,
                null,
                Instant.parse("2026-05-18T10:15:30Z"),
                Instant.parse("2026-05-18T10:20:30Z"),
                null
        );
        when(postRepository.findGlobalFeed(any(FeedQuery.class)))
                .thenReturn(new PageResult<>(List.of(post), 0, 20, 1, 1, false));

        ViewGlobalFeedResult result = useCase.execute(userId, 0, 20);

        ArgumentCaptor<FeedQuery> queryCaptor = ArgumentCaptor.forClass(FeedQuery.class);
        verify(postRepository).findGlobalFeed(queryCaptor.capture());
        assertThat(queryCaptor.getValue().page()).isEqualTo(0);
        assertThat(queryCaptor.getValue().size()).isEqualTo(20);
        assertThat(result.items()).hasSize(1);
        assertThat(result.items().getFirst().postId()).isEqualTo("507f1f77bcf86cd799439011");
        assertThat(result.items().getFirst().visibility()).isEqualTo("PUBLIC");
    }

    @Test
    void shouldThrowInvalidPaginationWhenSizeExceedsLimit() {
        UUID userId = UUID.randomUUID();

        assertThatThrownBy(() -> useCase.execute(userId, 0, 51))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> {
                    AppException appException = (AppException) ex;
                    assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.INVALID_PAGINATION);
                    assertThat(appException.getField()).isEqualTo("size");
                });
    }

    @Test
    void shouldThrowUnauthorizedWhenUserMissing() {
        assertThatThrownBy(() -> useCase.execute(null, 0, 20))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> {
                    AppException appException = (AppException) ex;
                    assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.UNAUTHORIZED);
                });
    }
}
