package com.twohands.social_service.application.comment.listpostcomments;

import com.twohands.social_service.application.post.common.PostIdValidator;
import com.twohands.social_service.application.post.common.PostViewAccessPolicy;
import com.twohands.social_service.application.post.common.PostViewAccessPolicy.PostViewAccessOutcome;
import com.twohands.social_service.domain.comment.Comment;
import com.twohands.social_service.domain.comment.CommentListQuery;
import com.twohands.social_service.domain.comment.CommentMediaItem;
import com.twohands.social_service.domain.comment.CommentRepository;
import com.twohands.social_service.domain.comment.CommentSortOrder;
import com.twohands.social_service.domain.follow.FollowRepository;
import com.twohands.social_service.domain.post.PageResult;
import com.twohands.social_service.domain.post.Post;
import com.twohands.social_service.domain.post.PostRepository;
import com.twohands.social_service.domain.user.UserProjection;
import com.twohands.social_service.domain.user.UserProjectionRepository;
import com.twohands.social_service.exception.AppException;
import com.twohands.social_service.exception.ErrorCode;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ListPostCommentsUseCase {

    private static final int MIN_PAGE = 0;
    private static final int MIN_SIZE = 1;
    private static final int MAX_SIZE = 50;
    private static final String DELETED_ACCOUNT_DISPLAY_NAME = "Tai khoan da xoa";
    private static final String SUCCESS_MESSAGE = "Lay danh sach binh luan thanh cong.";

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final FollowRepository followRepository;
    private final UserProjectionRepository userProjectionRepository;
    private final PostViewAccessPolicy postViewAccessPolicy;
    private final PostIdValidator postIdValidator;

    public ListPostCommentsUseCase(
            PostRepository postRepository,
            CommentRepository commentRepository,
            FollowRepository followRepository,
            UserProjectionRepository userProjectionRepository,
            PostViewAccessPolicy postViewAccessPolicy,
            PostIdValidator postIdValidator
    ) {
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
        this.followRepository = followRepository;
        this.userProjectionRepository = userProjectionRepository;
        this.postViewAccessPolicy = postViewAccessPolicy;
        this.postIdValidator = postIdValidator;
    }

    @Transactional(readOnly = true)
    public ListPostCommentsResult execute(
            UUID viewerId,
            String postId,
            int page,
            int size,
            String parentCommentId,
            String sort
    ) {
        requireAuthenticated(viewerId);
        postIdValidator.validate(postId);
        validatePagination(page, size);
        CommentSortOrder sortOrder = resolveSort(sort);
        validateParentCommentId(parentCommentId);

        Post post = postRepository.findById(postId).orElse(null);
        List<String> followeeAuthorIds = followRepository.findAcceptedFolloweeIds(viewerId).stream()
                .map(UUID::toString)
                .toList();

        PostViewAccessOutcome access = postViewAccessPolicy.evaluateViewAccess(post, viewerId, followeeAuthorIds);
        if (access == PostViewAccessOutcome.FORBIDDEN) {
            throw new AppException(ErrorCode.FORBIDDEN, "Ban khong co quyen xem bai viet nay.");
        }
        if (access != PostViewAccessOutcome.ALLOWED) {
            throw new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Bai viet khong ton tai.");
        }

        if (parentCommentId != null && !parentCommentId.isBlank()) {
            commentRepository.findActiveByIdAndPostId(parentCommentId, postId)
                    .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Comment cha khong ton tai."));
        }

        String normalizedParentId = normalizeParentCommentId(parentCommentId);
        PageResult<Comment> commentsPage = commentRepository.findActiveByPost(
                new CommentListQuery(postId, normalizedParentId, page, size, sortOrder)
        );

        boolean includeReplyCount = normalizedParentId == null;
        List<ListPostCommentsResult.CommentItem> items = commentsPage.items().stream()
                .map(comment -> toItem(comment, postId, includeReplyCount))
                .toList();

        return ListPostCommentsResult.from(new PageResult<>(
                items,
                commentsPage.page(),
                commentsPage.size(),
                commentsPage.totalElements(),
                commentsPage.totalPages(),
                commentsPage.hasNext()
        ));
    }

    public String successMessage() {
        return SUCCESS_MESSAGE;
    }

    private void requireAuthenticated(UUID viewerId) {
        if (viewerId == null) {
            throw new AppException(ErrorCode.UNAUTHORIZED, "Authentication required");
        }
    }

    private void validatePagination(int page, int size) {
        if (page < MIN_PAGE) {
            throw new AppException(
                    ErrorCode.INVALID_PAGINATION,
                    "Tham so pagination khong hop le.",
                    "page",
                    "MUST_BE_GREATER_THAN_OR_EQUAL_TO_0"
            );
        }
        if (size < MIN_SIZE || size > MAX_SIZE) {
            throw new AppException(
                    ErrorCode.INVALID_PAGINATION,
                    "Tham so pagination khong hop le.",
                    "size",
                    "MUST_BE_BETWEEN_1_AND_50"
            );
        }
    }

    private CommentSortOrder resolveSort(String sort) {
        CommentSortOrder sortOrder = CommentSortOrder.fromQueryParam(sort);
        if (sortOrder == null) {
            throw new AppException(ErrorCode.BAD_REQUEST, "sort khong hop le.");
        }
        return sortOrder;
    }

    private void validateParentCommentId(String parentCommentId) {
        if (parentCommentId == null || parentCommentId.isBlank()) {
            return;
        }
        if (!ObjectId.isValid(parentCommentId)) {
            throw new AppException(ErrorCode.BAD_REQUEST, "parent_comment_id khong hop le.");
        }
    }

    private String normalizeParentCommentId(String parentCommentId) {
        if (parentCommentId == null || parentCommentId.isBlank()) {
            return null;
        }
        return parentCommentId;
    }

    private ListPostCommentsResult.CommentItem toItem(Comment comment, String postId, boolean includeReplyCount) {
        long replyCount = includeReplyCount
                ? commentRepository.countActiveReplies(postId, comment.id())
                : 0L;
        List<ListPostCommentsResult.MediaItemData> media = comment.media().stream()
                .map(this::toMedia)
                .toList();

        return new ListPostCommentsResult.CommentItem(
                comment.id(),
                comment.postId(),
                comment.parentCommentId(),
                resolveAuthor(comment.authorId()),
                comment.contentText(),
                media,
                comment.likeCount(),
                replyCount,
                comment.createdAt() != null ? comment.createdAt().toString() : null,
                comment.updatedAt() != null ? comment.updatedAt().toString() : null
        );
    }

    private ListPostCommentsResult.MediaItemData toMedia(CommentMediaItem mediaItem) {
        return new ListPostCommentsResult.MediaItemData(mediaItem.url(), mediaItem.type());
    }

    private ListPostCommentsResult.AuthorSummary resolveAuthor(String authorId) {
        UUID authorUserId;
        try {
            authorUserId = UUID.fromString(authorId);
        } catch (IllegalArgumentException ex) {
            return new ListPostCommentsResult.AuthorSummary(authorId, "User", null);
        }

        return userProjectionRepository.findByUserId(authorUserId)
                .map(this::toAuthorSummary)
                .orElseGet(() -> new ListPostCommentsResult.AuthorSummary(authorId, "User", null));
    }

    private ListPostCommentsResult.AuthorSummary toAuthorSummary(UserProjection projection) {
        if (projection.isDeleted()) {
            return new ListPostCommentsResult.AuthorSummary(
                    projection.userId(),
                    DELETED_ACCOUNT_DISPLAY_NAME,
                    null
            );
        }
        return new ListPostCommentsResult.AuthorSummary(
                projection.userId(),
                projection.displayName(),
                projection.avatarUrl()
        );
    }
}
