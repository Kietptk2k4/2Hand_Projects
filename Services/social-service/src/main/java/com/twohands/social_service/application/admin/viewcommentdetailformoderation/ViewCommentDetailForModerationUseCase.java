package com.twohands.social_service.application.admin.viewcommentdetailformoderation;

import com.twohands.social_service.application.admin.common.AdminModerationAccessPolicy;
import com.twohands.social_service.application.admin.common.AdminModerationAuthorResolver;
import com.twohands.social_service.application.admin.common.PostMediaThumbnailResolver;
import com.twohands.social_service.application.comment.common.CommentIdValidator;
import com.twohands.social_service.application.post.common.PostIdValidator;
import com.twohands.social_service.domain.comment.Comment;
import com.twohands.social_service.domain.comment.CommentMediaItem;
import com.twohands.social_service.domain.comment.CommentModerationStatus;
import com.twohands.social_service.domain.comment.CommentRepository;
import com.twohands.social_service.domain.post.MediaItem;
import com.twohands.social_service.domain.post.Post;
import com.twohands.social_service.domain.post.PostModerationStatus;
import com.twohands.social_service.domain.post.PostRepository;
import com.twohands.social_service.exception.AppException;
import com.twohands.social_service.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ViewCommentDetailForModerationUseCase {

    private static final int CONTENT_PREVIEW_MAX_LENGTH = 120;
    private static final String SUCCESS_MESSAGE = "Lay chi tiet binh luan kiem duyet thanh cong.";

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final CommentIdValidator commentIdValidator;
    private final PostIdValidator postIdValidator;
    private final AdminModerationAuthorResolver authorResolver;

    public ViewCommentDetailForModerationUseCase(
            CommentRepository commentRepository,
            PostRepository postRepository,
            CommentIdValidator commentIdValidator,
            PostIdValidator postIdValidator,
            AdminModerationAuthorResolver authorResolver
    ) {
        this.commentRepository = commentRepository;
        this.postRepository = postRepository;
        this.commentIdValidator = commentIdValidator;
        this.postIdValidator = postIdValidator;
        this.authorResolver = authorResolver;
    }

    @Transactional(readOnly = true)
    public ViewCommentDetailForModerationResult execute(ViewCommentDetailForModerationCommand command) {
        AdminModerationAccessPolicy.ensureCanViewCommentList(command.actor());
        commentIdValidator.validate(command.commentId());

        Comment comment = commentRepository.findById(command.commentId())
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Binh luan khong ton tai."));

        AdminModerationAuthorResolver.AuthorSummary author = authorResolver.resolveAuthor(comment.authorId());
        List<CommentMediaItem> media = comment.media() == null ? List.of() : comment.media();
        ViewCommentDetailForModerationResult.ParentCommentSummary parentComment =
                resolveParentComment(comment.parentCommentId());
        ViewCommentDetailForModerationResult.PostContextSummary postContext =
                resolvePostContext(comment.postId());

        return new ViewCommentDetailForModerationResult(
                comment.id(),
                comment.postId(),
                new ViewCommentDetailForModerationResult.AuthorSummary(
                        author.userId() != null ? author.userId() : comment.authorId(),
                        author.displayName(),
                        author.avatarUrl()
                ),
                comment.parentCommentId(),
                parentComment,
                comment.contentText(),
                media.stream().map(this::toMedia).toList(),
                media.size(),
                comment.status().name(),
                resolveModerationStatus(comment.moderationStatusOrDefault()),
                comment.moderationReason(),
                comment.lastModerationLogId(),
                comment.likeCount(),
                postContext,
                comment.createdAt(),
                comment.updatedAt()
        );
    }

    public String successMessage() {
        return SUCCESS_MESSAGE;
    }

    private ViewCommentDetailForModerationResult.ParentCommentSummary resolveParentComment(String parentCommentId) {
        if (parentCommentId == null || parentCommentId.isBlank()) {
            return null;
        }
        return commentRepository.findById(parentCommentId)
                .map(parent -> new ViewCommentDetailForModerationResult.ParentCommentSummary(
                        parent.id(),
                        toPreview(parent.contentText())
                ))
                .orElse(null);
    }

    private ViewCommentDetailForModerationResult.PostContextSummary resolvePostContext(String postId) {
        if (postId == null || postId.isBlank()) {
            return null;
        }
        postIdValidator.validate(postId);
        return postRepository.findById(postId)
                .map(this::toPostContext)
                .orElse(null);
    }

    private ViewCommentDetailForModerationResult.PostContextSummary toPostContext(Post post) {
        List<MediaItem> media = post.media() == null ? List.of() : post.media();
        String moderationStatus = post.moderationStatusOrDefault() == null
                ? PostModerationStatus.NONE.name()
                : post.moderationStatusOrDefault().name();

        return new ViewCommentDetailForModerationResult.PostContextSummary(
                post.id(),
                toPreview(post.caption()),
                PostMediaThumbnailResolver.resolveThumbnailUrl(media),
                moderationStatus
        );
    }

    private ViewCommentDetailForModerationResult.MediaItemData toMedia(CommentMediaItem mediaItem) {
        return new ViewCommentDetailForModerationResult.MediaItemData(
                mediaItem.url(),
                mediaItem.type()
        );
    }

    private String resolveModerationStatus(CommentModerationStatus moderationStatus) {
        return moderationStatus == null ? CommentModerationStatus.NONE.name() : moderationStatus.name();
    }

    private String toPreview(String content) {
        if (content == null || content.isBlank()) {
            return "";
        }
        String trimmed = content.trim();
        if (trimmed.length() <= CONTENT_PREVIEW_MAX_LENGTH) {
            return trimmed;
        }
        return trimmed.substring(0, CONTENT_PREVIEW_MAX_LENGTH) + "...";
    }
}
