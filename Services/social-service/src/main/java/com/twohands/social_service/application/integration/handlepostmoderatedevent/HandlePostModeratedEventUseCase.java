package com.twohands.social_service.application.integration.handlepostmoderatedevent;

import com.twohands.social_service.domain.integration.ProcessedDomainEventRepository;
import com.twohands.social_service.domain.post.Post;
import com.twohands.social_service.domain.post.PostModerationAction;
import com.twohands.social_service.domain.post.PostModerationStatus;
import com.twohands.social_service.domain.post.PostRepository;
import com.twohands.social_service.domain.post.PostStatus;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class HandlePostModeratedEventUseCase {

    public static final String CONSUMER_NAME = "social-post-moderated";
    private static final String EVENT_TYPE = "POST_MODERATED";

    private static final Logger log = LoggerFactory.getLogger(HandlePostModeratedEventUseCase.class);

    private final ProcessedDomainEventRepository processedDomainEventRepository;
    private final PostRepository postRepository;

    public HandlePostModeratedEventUseCase(
            ProcessedDomainEventRepository processedDomainEventRepository,
            PostRepository postRepository
    ) {
        this.processedDomainEventRepository = processedDomainEventRepository;
        this.postRepository = postRepository;
    }

    @Transactional
    public HandlePostModeratedEventResult execute(HandlePostModeratedEventCommand command) {
        requireEventId(command.eventId());
        validatePostId(command.postId());
        requireAction(command.action());

        if (processedDomainEventRepository.existsByEventId(command.eventId())) {
            log.debug("Skip duplicate post moderated event. eventId={}, postId={}", command.eventId(), command.postId());
            return new HandlePostModeratedEventResult(
                    command.eventId(),
                    command.postId(),
                    command.action(),
                    true,
                    false
            );
        }

        Optional<Post> existing = postRepository.findById(command.postId());
        if (existing.isEmpty()) {
            log.warn("Post not found for moderation event. eventId={}, postId={}", command.eventId(), command.postId());
            markProcessed(command);
            return new HandlePostModeratedEventResult(
                    command.eventId(),
                    command.postId(),
                    command.action(),
                    false,
                    true
            );
        }

        Post post = existing.get();
        if (isDuplicateModeration(post, command)) {
            log.debug(
                    "Skip duplicate moderation application. eventId={}, postId={}, moderationLogId={}",
                    command.eventId(),
                    command.postId(),
                    command.moderationLogId()
            );
            markProcessed(command);
            return new HandlePostModeratedEventResult(
                    command.eventId(),
                    command.postId(),
                    command.action(),
                    true,
                    false
            );
        }

        if (command.action() == PostModerationAction.REMOVE && post.status() == PostStatus.DELETED) {
            markProcessed(command);
            return new HandlePostModeratedEventResult(
                    command.eventId(),
                    command.postId(),
                    command.action(),
                    true,
                    false
            );
        }

        Instant moderatedAt = command.moderatedAt() != null ? command.moderatedAt() : Instant.now();
        Post updated = switch (command.action()) {
            case HIDE -> applyHide(post, command, moderatedAt);
            case REMOVE -> applyRemove(post, command, moderatedAt);
        };

        postRepository.save(updated);
        markProcessed(command);

        log.info(
                "Applied post moderation. eventId={}, postId={}, action={}, moderationLogId={}",
                command.eventId(),
                command.postId(),
                command.action(),
                command.moderationLogId()
        );

        return new HandlePostModeratedEventResult(
                command.eventId(),
                command.postId(),
                command.action(),
                false,
                false
        );
    }

    private void requireEventId(UUID eventId) {
        if (eventId == null) {
            throw new InvalidPostModeratedEventException("event_id is required");
        }
    }

    private void validatePostId(String postId) {
        if (postId == null || postId.isBlank() || !ObjectId.isValid(postId)) {
            throw new InvalidPostModeratedEventException("post_id is invalid");
        }
    }

    private void requireAction(PostModerationAction action) {
        if (action == null) {
            throw new InvalidPostModeratedEventException("action must be HIDE or REMOVE");
        }
    }

    private boolean isDuplicateModeration(Post post, HandlePostModeratedEventCommand command) {
        if (command.moderationLogId() == null || post.lastModerationLogId() == null) {
            return false;
        }
        if (!command.moderationLogId().toString().equals(post.lastModerationLogId())) {
            return false;
        }
        return switch (command.action()) {
            case HIDE -> post.moderationStatusOrDefault() == PostModerationStatus.HIDDEN;
            case REMOVE -> post.status() == PostStatus.DELETED;
        };
    }

    private Post applyHide(Post post, HandlePostModeratedEventCommand command, Instant moderatedAt) {
        return new Post(
                post.id(),
                post.authorId(),
                post.caption(),
                post.media(),
                post.productTags(),
                PostStatus.ACTIVE,
                post.visibility(),
                post.likeCount(),
                post.replyCount(),
                post.hashtags(),
                post.allowComments(),
                PostModerationStatus.HIDDEN,
                command.reason(),
                moderationLogId(command),
                post.createdAt(),
                moderatedAt,
                null
        );
    }

    private Post applyRemove(Post post, HandlePostModeratedEventCommand command, Instant moderatedAt) {
        return new Post(
                post.id(),
                post.authorId(),
                post.caption(),
                post.media(),
                post.productTags(),
                PostStatus.DELETED,
                post.visibility(),
                post.likeCount(),
                post.replyCount(),
                post.hashtags(),
                post.allowComments(),
                PostModerationStatus.REMOVED,
                command.reason(),
                moderationLogId(command),
                post.createdAt(),
                moderatedAt,
                moderatedAt
        );
    }

    private String moderationLogId(HandlePostModeratedEventCommand command) {
        return command.moderationLogId() != null ? command.moderationLogId().toString() : null;
    }

    private void markProcessed(HandlePostModeratedEventCommand command) {
        processedDomainEventRepository.markProcessed(command.eventId(), CONSUMER_NAME, EVENT_TYPE);
    }
}
