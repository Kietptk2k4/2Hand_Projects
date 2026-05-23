package com.twohands.social_service.application.comment.deleteowncomment;

import com.twohands.social_service.domain.comment.Comment;
import com.twohands.social_service.domain.comment.CommentRepository;
import com.twohands.social_service.domain.comment.CommentStatus;
import com.twohands.social_service.domain.post.PostRepository;
import com.twohands.social_service.application.user.common.UserWriteGuard;
import com.twohands.social_service.exception.AppException;
import com.twohands.social_service.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
public class DeleteOwnCommentUseCase {

    private static final Set<String> MODERATION_ROLES = Set.of("MODERATOR", "ADMIN");

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserWriteGuard userWriteGuard;

    public DeleteOwnCommentUseCase(
            CommentRepository commentRepository,
            PostRepository postRepository,
            UserWriteGuard userWriteGuard
    ) {
        this.commentRepository = commentRepository;
        this.postRepository = postRepository;
        this.userWriteGuard = userWriteGuard;
    }

    @Transactional
    public DeleteOwnCommentResult execute(DeleteOwnCommentCommand command) {
        userWriteGuard.assertCanWrite(command.actorId(), command.actorRoles());

        Comment existing = commentRepository.findById(command.commentId())
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Comment khong ton tai."));

        if (!canDelete(existing, command)) {
            throw new AppException(ErrorCode.FORBIDDEN, "Ban khong co quyen xoa comment nay.");
        }

        if (existing.status() == CommentStatus.DELETED) {
            return toResult(existing);
        }

        Instant now = Instant.now();
        Comment deleted = new Comment(
                existing.id(),
                existing.postId(),
                existing.authorId(),
                existing.parentCommentId(),
                existing.contentText(),
                existing.media(),
                CommentStatus.DELETED,
                existing.likeCount(),
                existing.createdAt(),
                now,
                now
        );

        Comment saved = commentRepository.save(deleted);
        postRepository.decrementReplyCount(existing.postId());
        return toResult(saved);
    }

    public String successMessage() {
        return "Xoa comment thanh cong.";
    }

    private boolean canDelete(Comment comment, DeleteOwnCommentCommand command) {
        if (comment.authorId().equals(command.actorId().toString())) {
            return true;
        }
        if (command.actorRoles() == null) {
            return false;
        }
        return command.actorRoles().stream()
                .map(role -> role.toUpperCase(Locale.ROOT))
                .anyMatch(MODERATION_ROLES::contains);
    }

    private DeleteOwnCommentResult toResult(Comment comment) {
        return new DeleteOwnCommentResult(
                comment.id(),
                comment.postId(),
                comment.status().name(),
                comment.deletedAt() != null ? comment.deletedAt().toString() : null,
                comment.updatedAt() != null ? comment.updatedAt().toString() : null
        );
    }
}
