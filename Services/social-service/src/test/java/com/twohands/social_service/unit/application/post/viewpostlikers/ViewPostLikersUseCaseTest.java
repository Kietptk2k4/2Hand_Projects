package com.twohands.social_service.unit.application.post.viewpostlikers;

import com.twohands.social_service.application.post.common.PostIdValidator;
import com.twohands.social_service.application.post.common.PostViewAccessPolicy;
import com.twohands.social_service.application.post.viewpostlikers.ViewPostLikersUseCase;
import com.twohands.social_service.application.reaction.common.LikeUserEnricher;
import com.twohands.social_service.application.reaction.common.ViewLikeUsersResult;
import com.twohands.social_service.domain.follow.FollowRepository;
import com.twohands.social_service.domain.post.PageResult;
import com.twohands.social_service.domain.post.Post;
import com.twohands.social_service.domain.post.PostLikeEntry;
import com.twohands.social_service.domain.post.PostLikeRepository;
import com.twohands.social_service.domain.post.PostModerationStatus;
import com.twohands.social_service.domain.post.PostRepository;
import com.twohands.social_service.domain.post.PostStatus;
import com.twohands.social_service.domain.post.PostVisibility;
import com.twohands.social_service.domain.post.ProductTag;
import com.twohands.social_service.domain.user.UserProjectionRepository;
import com.twohands.social_service.exception.AppException;
import com.twohands.social_service.exception.ErrorCode;
import com.twohands.social_service.testsupport.UserProjectionTestFixtures;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ViewPostLikersUseCaseTest {

    private final PostRepository postRepository = mock(PostRepository.class);
    private final PostLikeRepository postLikeRepository = mock(PostLikeRepository.class);
    private final FollowRepository followRepository = mock(FollowRepository.class);
    private final UserProjectionRepository userProjectionRepository = mock(UserProjectionRepository.class);
    private final PostViewAccessPolicy postViewAccessPolicy = new PostViewAccessPolicy();
    private final PostIdValidator postIdValidator = new PostIdValidator();
    private final LikeUserEnricher likeUserEnricher = new LikeUserEnricher(userProjectionRepository);
    private final ViewPostLikersUseCase useCase = new ViewPostLikersUseCase(
            postRepository,
            postLikeRepository,
            followRepository,
            postViewAccessPolicy,
            postIdValidator,
            likeUserEnricher
    );

    @Test
    void shouldReturnEnrichedPostLikers() {
        UUID viewerId = UUID.randomUUID();
        UUID likerId = UUID.randomUUID();
        String postId = "507f1f77bcf86cd799439011";
        Instant likedAt = Instant.parse("2026-01-01T10:00:00Z");
        Post post = buildPost(postId, viewerId.toString(), PostStatus.ACTIVE, PostVisibility.PUBLIC);

        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(followRepository.findAcceptedFolloweeIds(viewerId)).thenReturn(List.of());
        when(postLikeRepository.findLikersByPostId(postId, 0, 20)).thenReturn(new PageResult<>(
                List.of(new PostLikeEntry(likerId, likedAt)),
                0,
                20,
                1,
                1,
                false
        ));
        when(userProjectionRepository.findByUserId(likerId))
                .thenReturn(UserProjectionTestFixtures.activeOptional(likerId));

        ViewLikeUsersResult result = useCase.execute(viewerId, postId, 0, 20);

        assertThat(result.items()).hasSize(1);
        assertThat(result.items().getFirst().userId()).isEqualTo(likerId.toString());
        assertThat(result.items().getFirst().displayName()).isEqualTo("User");
        assertThat(result.meta().totalElements()).isEqualTo(1);
    }

    @Test
    void shouldThrowForbiddenForFollowersPostWithoutAccess() {
        UUID viewerId = UUID.randomUUID();
        UUID authorId = UUID.randomUUID();
        String postId = "507f1f77bcf86cd799439011";
        Post post = buildPost(postId, authorId.toString(), PostStatus.ACTIVE, PostVisibility.FOLLOWERS);

        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(followRepository.findAcceptedFolloweeIds(viewerId)).thenReturn(List.of());

        assertThatThrownBy(() -> useCase.execute(viewerId, postId, 0, 20))
                .isInstanceOf(AppException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.FORBIDDEN);
    }

    @Test
    void shouldThrowUnauthorizedWhenViewerMissing() {
        assertThatThrownBy(() -> useCase.execute(null, "507f1f77bcf86cd799439011", 0, 20))
                .isInstanceOf(AppException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.UNAUTHORIZED);
    }

    private Post buildPost(String postId, String authorId, PostStatus status, PostVisibility visibility) {
        Instant now = Instant.parse("2026-05-18T10:15:30Z");
        return new Post(
                postId,
                authorId,
                "caption",
                List.of(),
                List.of(new ProductTag("product-1", new BigDecimal("1000"))),
                status,
                visibility,
                0L,
                0L,
                List.of("tag"),
                true,
                PostModerationStatus.NONE,
                null,
                null,
                now,
                now,
                null
        );
    }
}