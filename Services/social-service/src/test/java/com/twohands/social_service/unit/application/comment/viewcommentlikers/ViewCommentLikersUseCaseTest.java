package com.twohands.social_service.unit.application.comment.viewcommentlikers;

import com.twohands.social_service.application.comment.viewcommentlikers.ViewCommentLikersUseCase;
import com.twohands.social_service.application.post.common.PostIdValidator;
import com.twohands.social_service.application.post.common.PostViewAccessPolicy;
import com.twohands.social_service.application.reaction.common.LikeUserEnricher;
import com.twohands.social_service.application.reaction.common.ViewLikeUsersResult;
import com.twohands.social_service.domain.comment.Comment;
import com.twohands.social_service.domain.comment.CommentLikeEntry;
import com.twohands.social_service.domain.comment.CommentReactionRepository;
import com.twohands.social_service.domain.comment.CommentRepository;
import com.twohands.social_service.domain.comment.CommentStatus;
import com.twohands.social_service.domain.follow.FollowRepository;
import com.twohands.social_service.domain.post.PageResult;
import com.twohands.social_service.domain.post.Post;
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

class ViewCommentLikersUseCaseTest {

    private final CommentRepository commentRepository = mock(CommentRepository.class);
    private final CommentReactionRepository commentReactionRepository = mock(CommentReactionRepository.class);
    private final PostRepository postRepository = mock(PostRepository.class);
    private final FollowRepository followRepository = mock(FollowRepository.class);
    private final UserProjectionRepository userProjectionRepository = mock(UserProjectionRepository.class);
    private final PostViewAccessPolicy postViewAccessPolicy = new PostViewAccessPolicy();
    private final PostIdValidator postIdValidator = new PostIdValidator();
    private final LikeUserEnricher likeUserEnricher = new LikeUserEnricher(userProjectionRepository);
    private final ViewCommentLikersUseCase useCase = new ViewCommentLikersUseCase(
            commentRepository,
            commentReactionRepository,
            postRepository,
            followRepository,
            postViewAccessPolicy,
            postIdValidator,
            likeUserEnricher
    );

    @Test
    void shouldReturnEnrichedCommentLikers() {
        UUID viewerId = UUID.randomUUID();
        UUID likerId = UUID.randomUUID();
        String postId = "507f1f77bcf86cd799439011";
        String commentId = "507f1f77bcf86cd799439012";
        Instant likedAt = Instant.parse("2026-01-01T10:00:00Z");
        Comment comment = buildComment(commentId, postId, UUID.randomUUID().toString());
        Post post = buildPost(postId, viewerId.toString(), PostVisibility.PUBLIC);

        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(followRepository.findAcceptedFolloweeIds(viewerId)).thenReturn(List.of());
        when(commentReactionRepository.findLikersByCommentId(commentId, 0, 20)).thenReturn(new PageResult<>(
                List.of(new CommentLikeEntry(likerId, likedAt)),
                0,
                20,
                1,
                1,
                false
        ));
        when(userProjectionRepository.findByUserId(likerId))
                .thenReturn(UserProjectionTestFixtures.activeOptional(likerId));

        ViewLikeUsersResult result = useCase.execute(viewerId, commentId, 0, 20);

        assertThat(result.items()).hasSize(1);
        assertThat(result.items().getFirst().displayName()).isEqualTo("User");
        assertThat(result.meta().totalElements()).isEqualTo(1);
    }

    @Test
    void shouldThrowNotFoundWhenCommentDeleted() {
        UUID viewerId = UUID.randomUUID();
        String commentId = "507f1f77bcf86cd799439012";
        Instant now = Instant.parse("2026-05-21T10:00:00Z");
        Comment comment = new Comment(
                commentId,
                "507f1f77bcf86cd799439011",
                UUID.randomUUID().toString(),
                null,
                "text",
                List.of(),
                CommentStatus.DELETED,
                0L,
                now,
                now,
                now
        );

        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));

        assertThatThrownBy(() -> useCase.execute(viewerId, commentId, 0, 20))
                .isInstanceOf(AppException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.RESOURCE_NOT_FOUND);
    }

    @Test
    void shouldThrowBadRequestForInvalidCommentId() {
        assertThatThrownBy(() -> useCase.execute(UUID.randomUUID(), "invalid", 0, 20))
                .isInstanceOf(AppException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.BAD_REQUEST);
    }

    private Comment buildComment(String commentId, String postId, String authorId) {
        Instant now = Instant.parse("2026-05-21T10:00:00Z");
        return new Comment(
                commentId,
                postId,
                authorId,
                null,
                "text",
                List.of(),
                CommentStatus.ACTIVE,
                1L,
                now,
                now,
                null
        );
    }

    private Post buildPost(String postId, String authorId, PostVisibility visibility) {
        Instant now = Instant.parse("2026-05-18T10:15:30Z");
        return new Post(
                postId,
                authorId,
                "caption",
                List.of(),
                List.of(new ProductTag("product-1", new BigDecimal("1000"))),
                PostStatus.ACTIVE,
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