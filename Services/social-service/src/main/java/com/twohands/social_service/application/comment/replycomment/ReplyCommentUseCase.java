package com.twohands.social_service.application.comment.replycomment;

import com.twohands.social_service.application.comment.common.CommentAuthorResolver;
import com.twohands.social_service.application.comment.common.CommentCreatedOutboxService;
import com.twohands.social_service.domain.comment.Comment;
import com.twohands.social_service.domain.comment.CommentMediaItem;
import com.twohands.social_service.domain.comment.CommentRepository;
import com.twohands.social_service.domain.comment.CommentStatus;
import com.twohands.social_service.domain.outbox.OutboxEventRepository;
import com.twohands.social_service.domain.post.Post;
import com.twohands.social_service.domain.post.PostRepository;
import com.twohands.social_service.domain.post.PostStatus;
import com.twohands.social_service.application.user.common.UserWriteGuard;
import com.twohands.social_service.exception.AppException;
import com.twohands.social_service.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.regex.Pattern;

@Service
public class ReplyCommentUseCase {

    private static final int MAX_CONTENT_LENGTH = 2000;
    private static final int MAX_MEDIA_ITEMS = 5;
    private static final Pattern UNSAFE_CONTENT_PATTERN = Pattern.compile(
            "(?i)<\\s*script|javascript\\s*:|on\\w+\\s*="
    );

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final CommentCreatedOutboxService commentCreatedOutboxService;
    private final UserWriteGuard userWriteGuard;
    private final CommentAuthorResolver commentAuthorResolver;

    public ReplyCommentUseCase(
            CommentRepository commentRepository,
            PostRepository postRepository,
            OutboxEventRepository outboxEventRepository,
            CommentCreatedOutboxService commentCreatedOutboxService,
            UserWriteGuard userWriteGuard,
            CommentAuthorResolver commentAuthorResolver
    ) {
        this.commentRepository = commentRepository;
        this.postRepository = postRepository;
        this.outboxEventRepository = outboxEventRepository;
        this.commentCreatedOutboxService = commentCreatedOutboxService;
        this.userWriteGuard = userWriteGuard;
        this.commentAuthorResolver = commentAuthorResolver;
    }

    @Transactional
    public ReplyCommentResult execute(ReplyCommentCommand command) {
        userWriteGuard.assertCanWrite(command.authorId());
        validatePayload(command);

        Comment parent = commentRepository.findById(command.parentCommentId())
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Comment cha khong ton tai."));

        if (!parent.isPubliclyVisible()) {
            throw new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Comment cha da bi xoa, khong the tra loi.");
        }

        validateThreadDepth(parent);

        Post post = postRepository.findById(parent.postId())
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Bai viet khong ton tai."));

        validatePostAllowsReply(post);

        Instant now = Instant.now();
        List<CommentMediaItem> media = command.media() != null ? command.media() : List.of();

        Comment reply = new Comment(
                null,
                parent.postId(),
                command.authorId().toString(),
                parent.id(),
                command.contentText(),
                media,
                CommentStatus.ACTIVE,
                null,
                null,
                null,
                0L,
                now,
                now,
                null
        );

        Comment saved = commentRepository.save(reply);
        postRepository.incrementReplyCount(parent.postId());
        outboxEventRepository.save(commentCreatedOutboxService.build(saved, post.authorId(), now));

        return toResult(saved);
    }

    public String successMessage() {
        return "Tra loi comment thanh cong.";
    }

    private void validatePayload(ReplyCommentCommand command) {
        if (command.contentText() == null || command.contentText().isBlank()) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "Validation failed",
                    "contentText", "Noi dung tra loi khong duoc de trong.");
        }
        if (command.contentText().length() > MAX_CONTENT_LENGTH) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "Validation failed",
                    "contentText", "Noi dung tra loi khong duoc vuot qua " + MAX_CONTENT_LENGTH + " ky tu.");
        }
        if (UNSAFE_CONTENT_PATTERN.matcher(command.contentText()).find()) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "Validation failed",
                    "contentText", "Noi dung tra loi chua noi dung khong hop le.");
        }
        if (command.media() != null) {
            if (command.media().size() > MAX_MEDIA_ITEMS) {
                throw new AppException(ErrorCode.VALIDATION_ERROR, "Validation failed",
                        "media", "Khong duoc dinh kem qua " + MAX_MEDIA_ITEMS + " media items.");
            }
            for (CommentMediaItem item : command.media()) {
                if (item.url() == null || item.url().isBlank()) {
                    throw new AppException(ErrorCode.VALIDATION_ERROR, "Validation failed",
                            "media[].url", "URL media khong duoc de trong.");
                }
                if (!"IMAGE".equals(item.type()) && !"VIDEO".equals(item.type())) {
                    throw new AppException(ErrorCode.VALIDATION_ERROR, "Validation failed",
                            "media[].type", "Media type chi chap nhan IMAGE hoac VIDEO.");
                }
            }
        }
    }

    private void validateThreadDepth(Comment parent) {
        if (parent.parentCommentId() != null) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "Validation failed",
                    "parentCommentId", "Chi duoc tra loi comment cap mot (khong ho tro nested reply sau tang 2).");
        }
    }

    private void validatePostAllowsReply(Post post) {
        if (post.status() == PostStatus.DELETED) {
            throw new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Bai viet khong ton tai.");
        }
        if (post.status() != PostStatus.ACTIVE) {
            throw new AppException(ErrorCode.FORBIDDEN, "Bai viet chua duoc xuat ban, khong the tra loi comment.");
        }
        if (!post.allowComments()) {
            throw new AppException(ErrorCode.FORBIDDEN, "Bai viet da tat binh luan.");
        }
    }

    private ReplyCommentResult toResult(Comment comment) {
        return new ReplyCommentResult(
                comment.id(),
                comment.postId(),
                comment.parentCommentId(),
                comment.authorId(),
                commentAuthorResolver.resolve(comment.authorId()),
                comment.contentText(),
                comment.media(),
                comment.status().name(),
                comment.createdAt() != null ? comment.createdAt().toString() : null,
                comment.updatedAt() != null ? comment.updatedAt().toString() : null
        );
    }
}
