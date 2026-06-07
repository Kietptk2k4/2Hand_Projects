package com.twohands.social_service.application.comment.viewcommentlikers;

import com.twohands.social_service.application.post.common.PostIdValidator;
import com.twohands.social_service.application.post.common.PostViewAccessPolicy;
import com.twohands.social_service.application.post.common.PostViewAccessPolicy.PostViewAccessOutcome;
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
import com.twohands.social_service.domain.post.PostRepository;
import com.twohands.social_service.exception.AppException;
import com.twohands.social_service.exception.ErrorCode;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ViewCommentLikersUseCase {

    private static final int MIN_PAGE = 0;
    private static final int MIN_SIZE = 1;
    private static final int MAX_SIZE = 50;
    private static final String SUCCESS_MESSAGE = "Lay danh sach nguoi thich binh luan thanh cong.";

    private final CommentRepository commentRepository;
    private final CommentReactionRepository commentReactionRepository;
    private final PostRepository postRepository;
    private final FollowRepository followRepository;
    private final PostViewAccessPolicy postViewAccessPolicy;
    private final PostIdValidator postIdValidator;
    private final LikeUserEnricher likeUserEnricher;

    public ViewCommentLikersUseCase(
            CommentRepository commentRepository,
            CommentReactionRepository commentReactionRepository,
            PostRepository postRepository,
            FollowRepository followRepository,
            PostViewAccessPolicy postViewAccessPolicy,
            PostIdValidator postIdValidator,
            LikeUserEnricher likeUserEnricher
    ) {
        this.commentRepository = commentRepository;
        this.commentReactionRepository = commentReactionRepository;
        this.postRepository = postRepository;
        this.followRepository = followRepository;
        this.postViewAccessPolicy = postViewAccessPolicy;
        this.postIdValidator = postIdValidator;
        this.likeUserEnricher = likeUserEnricher;
    }

    @Transactional(readOnly = true)
    public ViewLikeUsersResult execute(UUID viewerId, String commentId, int page, int size) {
        requireAuthenticated(viewerId);
        validateCommentId(commentId);
        validatePagination(page, size);

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Comment khong ton tai."));
        if (comment.status() != CommentStatus.ACTIVE) {
            throw new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Comment khong ton tai hoac da bi xoa.");
        }

        String postId = comment.postId();
        postIdValidator.validate(postId);

        Post post = postRepository.findById(postId).orElse(null);
        List<String> followeeAuthorIds = followRepository.findAcceptedFolloweeIds(viewerId).stream()
                .map(UUID::toString)
                .toList();

        PostViewAccessOutcome access = postViewAccessPolicy.evaluateViewAccess(post, viewerId, followeeAuthorIds);
        if (access == PostViewAccessOutcome.FORBIDDEN) {
            throw new AppException(ErrorCode.FORBIDDEN, "Ban khong co quyen xem binh luan nay.");
        }
        if (access != PostViewAccessOutcome.ALLOWED) {
            throw new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Comment khong ton tai.");
        }

        PageResult<CommentLikeEntry> likersPage = commentReactionRepository.findLikersByCommentId(
                commentId,
                page,
                size
        );
        List<ViewLikeUsersResult.LikeUserItem> items = likersPage.items().stream()
                .map(entry -> likeUserEnricher.enrich(entry.userId(), entry.likedAt()))
                .toList();

        return ViewLikeUsersResult.from(new PageResult<>(
                items,
                likersPage.page(),
                likersPage.size(),
                likersPage.totalElements(),
                likersPage.totalPages(),
                likersPage.hasNext()
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

    private void validateCommentId(String commentId) {
        if (commentId == null || commentId.isBlank() || !ObjectId.isValid(commentId)) {
            throw new AppException(ErrorCode.BAD_REQUEST, "comment_id khong hop le.");
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
}