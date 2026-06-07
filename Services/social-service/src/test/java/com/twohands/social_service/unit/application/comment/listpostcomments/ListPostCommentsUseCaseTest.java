package com.twohands.social_service.unit.application.comment.listpostcomments;

import com.twohands.social_service.application.comment.listpostcomments.ListPostCommentsResult;
import com.twohands.social_service.application.comment.listpostcomments.ListPostCommentsUseCase;
import com.twohands.social_service.application.post.common.PostIdValidator;
import com.twohands.social_service.application.post.common.PostViewAccessPolicy;
import com.twohands.social_service.domain.comment.Comment;
import com.twohands.social_service.domain.comment.CommentListQuery;
import com.twohands.social_service.domain.comment.CommentReactionRepository;
import com.twohands.social_service.domain.comment.CommentRepository;
import com.twohands.social_service.domain.comment.CommentSortOrder;
import com.twohands.social_service.domain.comment.CommentStatus;
import com.twohands.social_service.domain.follow.FollowRepository;
import com.twohands.social_service.domain.post.PageResult;
import com.twohands.social_service.domain.post.Post;
import com.twohands.social_service.domain.post.ProductTag;
import com.twohands.social_service.domain.post.PostRepository;
import com.twohands.social_service.domain.post.PostModerationStatus;
import com.twohands.social_service.domain.post.PostStatus;
import com.twohands.social_service.domain.post.PostVisibility;
import com.twohands.social_service.domain.user.UserProjectionRepository;
import com.twohands.social_service.exception.AppException;
import com.twohands.social_service.exception.ErrorCode;
import com.twohands.social_service.testsupport.UserProjectionTestFixtures;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ListPostCommentsUseCaseTest {

    private final PostRepository postRepository = org.mockito.Mockito.mock(PostRepository.class);
    private final CommentRepository commentRepository = org.mockito.Mockito.mock(CommentRepository.class);
    private final CommentReactionRepository commentReactionRepository =
            org.mockito.Mockito.mock(CommentReactionRepository.class);
    private final FollowRepository followRepository = org.mockito.Mockito.mock(FollowRepository.class);
    private final UserProjectionRepository userProjectionRepository = org.mockito.Mockito.mock(UserProjectionRepository.class);
    private final PostViewAccessPolicy postViewAccessPolicy = new PostViewAccessPolicy();
    private final PostIdValidator postIdValidator = new PostIdValidator();
    private final ListPostCommentsUseCase useCase = new ListPostCommentsUseCase(
            postRepository,
            commentRepository,
            commentReactionRepository,
            followRepository,
            userProjectionRepository,
            postViewAccessPolicy,
            postIdValidator
    );

    @Test
    void shouldReturnTopLevelCommentsWithReplyCount() {
        UUID viewerId = UUID.randomUUID();
        UUID authorId = UUID.randomUUID();
        String postId = "507f1f77bcf86cd799439011";
        String commentId = "507f1f77bcf86cd799439012";
        Post post = buildPost(postId, authorId.toString(), PostStatus.ACTIVE, PostVisibility.PUBLIC);
        Comment comment = buildComment(commentId, postId, null, authorId.toString());

        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(followRepository.findAcceptedFolloweeIds(viewerId)).thenReturn(List.of());
        when(commentRepository.findActiveByPost(any(CommentListQuery.class))).thenReturn(new PageResult<>(
                List.of(comment),
                0,
                20,
                1,
                1,
                false
        ));
        when(commentRepository.countActiveReplies(postId, commentId)).thenReturn(3L);
        when(userProjectionRepository.findByUserId(authorId)).thenReturn(UserProjectionTestFixtures.activeOptional(authorId));
        when(commentReactionRepository.findLikedCommentIdsByUserIdAndCommentIds(viewerId, List.of(commentId)))
                .thenReturn(Set.of(commentId));

        ListPostCommentsResult result = useCase.execute(viewerId, postId, 0, 20, null, "created_at_asc");

        assertThat(result.items()).hasSize(1);
        assertThat(result.items().getFirst().commentId()).isEqualTo(commentId);
        assertThat(result.items().getFirst().replyCount()).isEqualTo(3L);
        assertThat(result.items().getFirst().likedByMe()).isTrue();
        assertThat(result.items().getFirst().author().displayName()).isEqualTo("User");
        assertThat(result.meta().totalElements()).isEqualTo(1);
        verify(commentRepository).findActiveByPost(new CommentListQuery(
                postId,
                null,
                0,
                20,
                CommentSortOrder.CREATED_AT_ASC
        ));
    }

    @Test
    void shouldReturnRepliesWhenParentCommentIdProvided() {
        UUID viewerId = UUID.randomUUID();
        UUID authorId = UUID.randomUUID();
        String postId = "507f1f77bcf86cd799439011";
        String parentCommentId = "507f1f77bcf86cd799439012";
        String replyId = "507f1f77bcf86cd799439013";
        Post post = buildPost(postId, authorId.toString(), PostStatus.ACTIVE, PostVisibility.PUBLIC);
        Comment parent = buildComment(parentCommentId, postId, null, authorId.toString());
        Comment reply = buildComment(replyId, postId, parentCommentId, authorId.toString());

        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(followRepository.findAcceptedFolloweeIds(viewerId)).thenReturn(List.of());
        when(commentRepository.findActiveByIdAndPostId(parentCommentId, postId)).thenReturn(Optional.of(parent));
        when(commentRepository.findActiveByPost(any(CommentListQuery.class))).thenReturn(new PageResult<>(
                List.of(reply),
                0,
                20,
                1,
                1,
                false
        ));
        when(userProjectionRepository.findByUserId(authorId)).thenReturn(UserProjectionTestFixtures.activeOptional(authorId));
        when(commentReactionRepository.findLikedCommentIdsByUserIdAndCommentIds(viewerId, List.of(replyId)))
                .thenReturn(Set.of());

        ListPostCommentsResult result = useCase.execute(viewerId, postId, 0, 20, parentCommentId, "created_at_desc");

        assertThat(result.items()).hasSize(1);
        assertThat(result.items().getFirst().parentCommentId()).isEqualTo(parentCommentId);
        assertThat(result.items().getFirst().replyCount()).isZero();
        verify(commentRepository).findActiveByPost(new CommentListQuery(
                postId,
                parentCommentId,
                0,
                20,
                CommentSortOrder.CREATED_AT_DESC
        ));
    }

    @Test
    void shouldThrowNotFoundWhenPostMissing() {
        UUID viewerId = UUID.randomUUID();
        String postId = "507f1f77bcf86cd799439011";

        when(postRepository.findById(postId)).thenReturn(Optional.empty());
        when(followRepository.findAcceptedFolloweeIds(viewerId)).thenReturn(List.of());

        assertThatThrownBy(() -> useCase.execute(viewerId, postId, 0, 20, null, "created_at_asc"))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode()).isEqualTo(ErrorCode.RESOURCE_NOT_FOUND));
    }

    @Test
    void shouldThrowForbiddenWhenPostNotViewable() {
        UUID viewerId = UUID.randomUUID();
        UUID authorId = UUID.randomUUID();
        String postId = "507f1f77bcf86cd799439011";
        Post post = buildPost(postId, authorId.toString(), PostStatus.ACTIVE, PostVisibility.FOLLOWERS);

        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(followRepository.findAcceptedFolloweeIds(viewerId)).thenReturn(List.of());

        assertThatThrownBy(() -> useCase.execute(viewerId, postId, 0, 20, null, "created_at_asc"))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode()).isEqualTo(ErrorCode.FORBIDDEN));
    }

    @Test
    void shouldThrowNotFoundWhenParentCommentMissing() {
        UUID viewerId = UUID.randomUUID();
        UUID authorId = UUID.randomUUID();
        String postId = "507f1f77bcf86cd799439011";
        String parentCommentId = "507f1f77bcf86cd799439012";
        Post post = buildPost(postId, authorId.toString(), PostStatus.ACTIVE, PostVisibility.PUBLIC);

        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(followRepository.findAcceptedFolloweeIds(viewerId)).thenReturn(List.of());
        when(commentRepository.findActiveByIdAndPostId(parentCommentId, postId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(viewerId, postId, 0, 20, parentCommentId, "created_at_asc"))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode()).isEqualTo(ErrorCode.RESOURCE_NOT_FOUND));
    }

    @Test
    void shouldThrowPaginationErrorWhenSizeTooLarge() {
        UUID viewerId = UUID.randomUUID();

        assertThatThrownBy(() -> useCase.execute(viewerId, "507f1f77bcf86cd799439011", 0, 51, null, "created_at_asc"))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode()).isEqualTo(ErrorCode.INVALID_PAGINATION));
    }

    @Test
    void shouldShowDeletedAccountLabelForDeletedAuthor() {
        UUID viewerId = UUID.randomUUID();
        UUID authorId = UUID.randomUUID();
        String postId = "507f1f77bcf86cd799439011";
        String commentId = "507f1f77bcf86cd799439012";
        Post post = buildPost(postId, UUID.randomUUID().toString(), PostStatus.ACTIVE, PostVisibility.PUBLIC);
        Comment comment = buildComment(commentId, postId, null, authorId.toString());

        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(followRepository.findAcceptedFolloweeIds(viewerId)).thenReturn(List.of());
        when(commentRepository.findActiveByPost(any(CommentListQuery.class))).thenReturn(new PageResult<>(
                List.of(comment),
                0,
                20,
                1,
                1,
                false
        ));
        when(commentRepository.countActiveReplies(eq(postId), eq(commentId))).thenReturn(0L);
        when(userProjectionRepository.findByUserId(authorId))
                .thenReturn(UserProjectionTestFixtures.deletedOptional(authorId));

        ListPostCommentsResult result = useCase.execute(viewerId, postId, 0, 20, null, "created_at_asc");

        assertThat(result.items().getFirst().author().displayName()).isEqualTo("Tai khoan da xoa");
        assertThat(result.items().getFirst().author().avatarUrl()).isNull();
    }

    private Post buildPost(String postId, String authorId, PostStatus status, PostVisibility visibility) {
        Instant now = Instant.parse("2026-05-18T10:15:30Z");
        return new Post(
                postId,
                authorId,
                "caption",
                List.of(),
                List.of(new ProductTag("product-1", new BigDecimal("100000"))),
                status,
                visibility,
                0L,
                0L,
                List.of(),
                true,
                PostModerationStatus.NONE,
                null,
                null,
                now,
                now,
                status == PostStatus.DELETED ? Instant.parse("2026-05-19T10:00:00Z") : null
        );
    }

    private Comment buildComment(String commentId, String postId, String parentCommentId, String authorId) {
        Instant now = Instant.parse("2026-05-21T10:00:00Z");
        return new Comment(
                commentId,
                postId,
                authorId,
                parentCommentId,
                "Hay qua!",
                List.of(),
                CommentStatus.ACTIVE,
                3L,
                now,
                now,
                null
        );
    }
}
