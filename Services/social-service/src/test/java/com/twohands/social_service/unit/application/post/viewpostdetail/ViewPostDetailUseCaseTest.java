package com.twohands.social_service.unit.application.post.viewpostdetail;

import com.twohands.social_service.application.post.common.PostIdValidator;
import com.twohands.social_service.application.post.common.PostViewAccessPolicy;
import com.twohands.social_service.application.post.viewpostdetail.ViewPostDetailResult;
import com.twohands.social_service.application.post.viewpostdetail.ViewPostDetailUseCase;
import com.twohands.social_service.domain.follow.FollowRepository;
import com.twohands.social_service.domain.post.MediaItem;
import com.twohands.social_service.domain.post.Post;
import com.twohands.social_service.domain.post.PostModerationStatus;
import com.twohands.social_service.domain.post.PostLikeRepository;
import com.twohands.social_service.domain.post.PostRepository;
import com.twohands.social_service.domain.post.PostSaveRepository;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ViewPostDetailUseCaseTest {

    private final PostRepository postRepository = org.mockito.Mockito.mock(PostRepository.class);
    private final PostLikeRepository postLikeRepository = org.mockito.Mockito.mock(PostLikeRepository.class);
    private final PostSaveRepository postSaveRepository = org.mockito.Mockito.mock(PostSaveRepository.class);
    private final FollowRepository followRepository = org.mockito.Mockito.mock(FollowRepository.class);
    private final UserProjectionRepository userProjectionRepository = org.mockito.Mockito.mock(UserProjectionRepository.class);
    private final PostViewAccessPolicy postViewAccessPolicy = new PostViewAccessPolicy();
    private final PostIdValidator postIdValidator = new PostIdValidator();
    private final ViewPostDetailUseCase useCase = new ViewPostDetailUseCase(
            postRepository,
            postLikeRepository,
            postSaveRepository,
            followRepository,
            userProjectionRepository,
            postViewAccessPolicy,
            postIdValidator
    );

    @Test
    void shouldReturnPublicActivePostWithEngagementFlags() {
        UUID viewerId = UUID.randomUUID();
        UUID authorId = UUID.randomUUID();
        String postId = "507f1f77bcf86cd799439011";
        Post post = buildPost(postId, authorId.toString(), PostStatus.ACTIVE, PostVisibility.PUBLIC);

        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(followRepository.findAcceptedFolloweeIds(viewerId)).thenReturn(List.of());
        when(userProjectionRepository.findByUserId(authorId)).thenReturn(UserProjectionTestFixtures.activeOptional(authorId));
        when(postLikeRepository.existsByPostIdAndUserId(postId, viewerId)).thenReturn(true);
        when(postSaveRepository.existsByPostIdAndUserId(postId, viewerId)).thenReturn(false);

        ViewPostDetailResult result = useCase.execute(viewerId, postId);

        assertThat(result.postId()).isEqualTo(postId);
        assertThat(result.author().userId()).isEqualTo(authorId.toString());
        assertThat(result.author().displayName()).isEqualTo("User");
        assertThat(result.visibility()).isEqualTo("PUBLIC");
        assertThat(result.status()).isEqualTo("ACTIVE");
        assertThat(result.likedByMe()).isTrue();
        assertThat(result.savedByMe()).isFalse();
        assertThat(result.isOwner()).isFalse();
        assertThat(result.productTags()).hasSize(1);
        assertThat(result.productTags().getFirst().productId()).isEqualTo("product-1");
        verify(postLikeRepository).existsByPostIdAndUserId(postId, viewerId);
        verify(postSaveRepository).existsByPostIdAndUserId(postId, viewerId);
    }

    @Test
    void shouldReturnFollowersPostWhenViewerFollowsAuthor() {
        UUID viewerId = UUID.randomUUID();
        UUID authorId = UUID.randomUUID();
        String postId = "507f1f77bcf86cd799439012";
        Post post = buildPost(postId, authorId.toString(), PostStatus.ACTIVE, PostVisibility.FOLLOWERS);

        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(followRepository.findAcceptedFolloweeIds(viewerId)).thenReturn(List.of(authorId));
        when(userProjectionRepository.findByUserId(authorId)).thenReturn(UserProjectionTestFixtures.activeOptional(authorId));
        when(postLikeRepository.existsByPostIdAndUserId(postId, viewerId)).thenReturn(false);
        when(postSaveRepository.existsByPostIdAndUserId(postId, viewerId)).thenReturn(false);

        ViewPostDetailResult result = useCase.execute(viewerId, postId);

        assertThat(result.visibility()).isEqualTo("FOLLOWERS");
    }

    @Test
    void shouldReturnDraftPostForAuthor() {
        UUID authorId = UUID.randomUUID();
        String postId = "507f1f77bcf86cd799439013";
        Post post = buildPost(postId, authorId.toString(), PostStatus.DRAFT, PostVisibility.PUBLIC);

        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(followRepository.findAcceptedFolloweeIds(authorId)).thenReturn(List.of());
        when(userProjectionRepository.findByUserId(authorId)).thenReturn(UserProjectionTestFixtures.activeOptional(authorId));
        when(postLikeRepository.existsByPostIdAndUserId(postId, authorId)).thenReturn(false);
        when(postSaveRepository.existsByPostIdAndUserId(postId, authorId)).thenReturn(false);

        ViewPostDetailResult result = useCase.execute(authorId, postId);

        assertThat(result.status()).isEqualTo("DRAFT");
        assertThat(result.isOwner()).isTrue();
    }

    @Test
    void shouldThrowForbiddenWhenFollowersPostAndViewerDoesNotFollow() {
        UUID viewerId = UUID.randomUUID();
        UUID authorId = UUID.randomUUID();
        String postId = "507f1f77bcf86cd799439014";
        Post post = buildPost(postId, authorId.toString(), PostStatus.ACTIVE, PostVisibility.FOLLOWERS);

        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(followRepository.findAcceptedFolloweeIds(viewerId)).thenReturn(List.of());

        assertThatThrownBy(() -> useCase.execute(viewerId, postId))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode()).isEqualTo(ErrorCode.FORBIDDEN));
    }

    @Test
    void shouldThrowNotFoundWhenPostDeleted() {
        UUID viewerId = UUID.randomUUID();
        String postId = "507f1f77bcf86cd799439015";
        Post post = buildPost(postId, UUID.randomUUID().toString(), PostStatus.DELETED, PostVisibility.PUBLIC);

        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(followRepository.findAcceptedFolloweeIds(viewerId)).thenReturn(List.of());

        assertThatThrownBy(() -> useCase.execute(viewerId, postId))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode()).isEqualTo(ErrorCode.RESOURCE_NOT_FOUND));
    }

    @Test
    void shouldThrowNotFoundWhenDraftViewedByNonAuthor() {
        UUID viewerId = UUID.randomUUID();
        String postId = "507f1f77bcf86cd799439016";
        Post post = buildPost(postId, UUID.randomUUID().toString(), PostStatus.DRAFT, PostVisibility.PUBLIC);

        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(followRepository.findAcceptedFolloweeIds(viewerId)).thenReturn(List.of());

        assertThatThrownBy(() -> useCase.execute(viewerId, postId))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode()).isEqualTo(ErrorCode.RESOURCE_NOT_FOUND));
    }

    @Test
    void shouldThrowBadRequestWhenPostIdInvalid() {
        UUID viewerId = UUID.randomUUID();

        assertThatThrownBy(() -> useCase.execute(viewerId, "not-an-object-id"))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode()).isEqualTo(ErrorCode.BAD_REQUEST));
    }

    @Test
    void shouldThrowUnauthorizedWhenViewerMissing() {
        assertThatThrownBy(() -> useCase.execute(null, "507f1f77bcf86cd799439011"))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode()).isEqualTo(ErrorCode.UNAUTHORIZED));
    }

    @Test
    void shouldUseFallbackAuthorWhenProjectionMissing() {
        UUID viewerId = UUID.randomUUID();
        UUID authorId = UUID.randomUUID();
        String postId = "507f1f77bcf86cd799439017";
        Post post = buildPost(postId, authorId.toString(), PostStatus.ACTIVE, PostVisibility.PUBLIC);

        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(followRepository.findAcceptedFolloweeIds(viewerId)).thenReturn(List.of());
        when(userProjectionRepository.findByUserId(authorId)).thenReturn(Optional.empty());
        when(postLikeRepository.existsByPostIdAndUserId(postId, viewerId)).thenReturn(false);
        when(postSaveRepository.existsByPostIdAndUserId(postId, viewerId)).thenReturn(false);

        ViewPostDetailResult result = useCase.execute(viewerId, postId);

        assertThat(result.author().displayName()).isEqualTo("User");
        assertThat(result.author().avatarUrl()).isNull();
    }

    private Post buildPost(String postId, String authorId, PostStatus status, PostVisibility visibility) {
        return new Post(
                postId,
                authorId,
                "caption",
                List.of(new MediaItem("https://cdn/1.jpg", "IMAGE")),
                List.of(new ProductTag("product-1", new BigDecimal("199000"))),
                status,
                visibility,
                10,
                2,
                List.of("tag"),
                true,
                PostModerationStatus.NONE,
                null,
                null,
                Instant.parse("2026-05-18T10:15:30Z"),
                Instant.parse("2026-05-18T10:20:30Z"),
                status == PostStatus.DELETED ? Instant.parse("2026-05-19T10:00:00Z") : null
        );
    }
}
