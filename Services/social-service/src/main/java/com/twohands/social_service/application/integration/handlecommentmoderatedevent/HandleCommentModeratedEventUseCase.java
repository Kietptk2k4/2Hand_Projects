package com.twohands.social_service.application.integration.handlecommentmoderatedevent;

import com.twohands.social_service.domain.comment.Comment;
import com.twohands.social_service.domain.comment.CommentModerationAction;
import com.twohands.social_service.domain.comment.CommentModerationStatus;
import com.twohands.social_service.domain.comment.CommentRepository;
import com.twohands.social_service.domain.comment.CommentStatus;
import com.twohands.social_service.domain.integration.ProcessedDomainEventRepository;
import com.twohands.social_service.domain.post.PostRepository;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class HandleCommentModeratedEventUseCase {

    public static final String CONSUMER_NAME = "social-comment-moderated";
    private static final String EVENT_TYPE = "COMMENT_MODERATED";

    private static final Logger log = LoggerFactory.getLogger(HandleCommentModeratedEventUseCase.class);

    private final ProcessedDomainEventRepository processedDomainEventRepository;
    private final CommentRepository commentRepository;
    private final PostRepository postRepository;

    public HandleCommentModeratedEventUseCase(
            ProcessedDomainEventRepository processedDomainEventRepository,
            CommentRepository commentRepository,
            PostRepository postRepository
    ) {
        this.processedDomainEventRepository = processedDomainEventRepository;
        this.commentRepository = commentRepository;
        this.postRepository = postRepository;
    }

    @Transactional
    public HandleCommentModeratedEventResult execute(HandleCommentModeratedEventCommand command) {
        requireEventId(command.eventId());
        validateCommentId(command.commentId());
        requireAction(command.action());

        if (processedDomainEventRepository.existsByEventId(command.eventId())) {
            log.debug(
                    "Skip duplicate comment moderated event. eventId={}, commentId={}",
                    command.eventId(),
                    command.commentId()
            );
            return new HandleCommentModeratedEventResult(
                    command.eventId(),
                    command.commentId(),
                    command.action(),
                    true,
                    false
            );
        }

        Optional<Comment> existing = commentRepository.findById(command.commentId());
        if (existing.isEmpty()) {
            log.warn(
                    "Comment not found for moderation event. eventId={}, commentId={}",
                    command.eventId(),
                    command.commentId()
            );
            markProcessed(command);
            return new HandleCommentModeratedEventResult(
                    command.eventId(),
                    command.commentId(),
                    command.action(),
                    false,
                    true
            );
        }

        Comment comment = existing.get();
        if (isDuplicateModeration(comment, command)) {
            log.debug(
                    "Skip duplicate moderation application. eventId={}, commentId={}, moderationLogId={}",
                    command.eventId(),
                    command.commentId(),
                    command.moderationLogId()
            );
            markProcessed(command);
            return new HandleCommentModeratedEventResult(
                    command.eventId(),
                    command.commentId(),
                    command.action(),
                    true,
                    false
            );
        }

        if (command.action() == CommentModerationAction.REMOVE && comment.status() == CommentStatus.DELETED) {
            markProcessed(command);
            return new HandleCommentModeratedEventResult(
                    command.eventId(),
                    command.commentId(),
                    command.action(),
                    true,
                    false
            );
        }

        Instant moderatedAt = command.moderatedAt() != null ? command.moderatedAt() : Instant.now();
        boolean wasPubliclyVisible = comment.isPubliclyVisible();
        Comment updated = switch (command.action()) {
            case HIDE -> applyHide(comment, command, moderatedAt);
            case REMOVE -> applyRemove(comment, command, moderatedAt);
        };

        commentRepository.save(updated);
        if (command.action() == CommentModerationAction.REMOVE && wasPubliclyVisible) {
            postRepository.decrementReplyCount(comment.postId());
        }
        markProcessed(command);

        log.info(
                "Applied comment moderation. eventId={}, commentId={}, action={}, moderationLogId={}",
                command.eventId(),
                command.commentId(),
                command.action(),
                command.moderationLogId()
        );

        return new HandleCommentModeratedEventResult(
                command.eventId(),
                command.commentId(),
                command.action(),
                false,
                false
        );
    }

    private void requireEventId(UUID eventId) {
        if (eventId == null) {
            throw new InvalidCommentModeratedEventException("event_id is required");
        }
    }

    private void validateCommentId(String commentId) {
        if (commentId == null || commentId.isBlank() || !ObjectId.isValid(commentId)) {
            throw new InvalidCommentModeratedEventException("comment_id is invalid");
        }
    }

    private void requireAction(CommentModerationAction action) {
        if (action == null) {
            throw new InvalidCommentModeratedEventException("action must be HIDE or REMOVE");
        }
    }

    private boolean isDuplicateModeration(Comment comment, HandleCommentModeratedEventCommand command) {
        if (command.moderationLogId() == null || comment.lastModerationLogId() == null) {
            return false;
        }
        if (!command.moderationLogId().toString().equals(comment.lastModerationLogId())) {
            return false;
        }
        return switch (command.action()) {
            case HIDE -> comment.moderationStatusOrDefault() == CommentModerationStatus.HIDDEN;
            case REMOVE -> comment.status() == CommentStatus.DELETED;
        };
    }

    private Comment applyHide(Comment comment, HandleCommentModeratedEventCommand command, Instant moderatedAt) {
        return new Comment(
                comment.id(),
                comment.postId(),
                comment.authorId(),
                comment.parentCommentId(),
                comment.contentText(),
                comment.media(),
                CommentStatus.ACTIVE,
                CommentModerationStatus.HIDDEN,
                command.reason(),
                moderationLogId(command),
                comment.likeCount(),
                comment.createdAt(),
                moderatedAt,
                comment.deletedAt()
        );
    }

    private Comment applyRemove(Comment comment, HandleCommentModeratedEventCommand command, Instant moderatedAt) {
        return new Comment(
                comment.id(),
                comment.postId(),
                comment.authorId(),
                comment.parentCommentId(),
                comment.contentText(),
                comment.media(),
                CommentStatus.DELETED,
                CommentModerationStatus.REMOVED,
                command.reason(),
                moderationLogId(command),
                comment.likeCount(),
                comment.createdAt(),
                moderatedAt,
                moderatedAt
        );
    }

    private String moderationLogId(HandleCommentModeratedEventCommand command) {
        return command.moderationLogId() != null ? command.moderationLogId().toString() : null;
    }

    private void markProcessed(HandleCommentModeratedEventCommand command) {
        processedDomainEventRepository.markProcessed(command.eventId(), CONSUMER_NAME, EVENT_TYPE);
    }
}
