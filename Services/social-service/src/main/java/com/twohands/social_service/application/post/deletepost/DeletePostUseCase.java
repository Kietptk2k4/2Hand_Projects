package com.twohands.social_service.application.post.deletepost;

import com.twohands.social_service.domain.post.Post;
import com.twohands.social_service.domain.post.PostRepository;
import com.twohands.social_service.domain.post.PostStatus;
import com.twohands.social_service.domain.user.UserProjectionRepository;
import com.twohands.social_service.exception.AppException;
import com.twohands.social_service.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
public class DeletePostUseCase {

    private static final Set<String> MODERATION_ROLES = Set.of("MODERATOR", "ADMIN");

    private final PostRepository postRepository;
    private final UserProjectionRepository userProjectionRepository;

    public DeletePostUseCase(PostRepository postRepository, UserProjectionRepository userProjectionRepository) {
        this.postRepository = postRepository;
        this.userProjectionRepository = userProjectionRepository;
    }

    @Transactional
    public DeletePostResult execute(DeletePostCommand command) {
        validateActor(command);

        Post existing = postRepository.findById(command.postId())
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Bai viet khong ton tai."));

        if (!canDelete(existing, command)) {
            throw new AppException(ErrorCode.FORBIDDEN, "Ban khong co quyen xoa bai viet nay.");
        }

        if (existing.status() == PostStatus.DELETED) {
            return toResult(existing);
        }

        Instant now = Instant.now();
        Post deleted = new Post(
                existing.id(),
                existing.authorId(),
                existing.caption(),
                existing.media(),
                existing.productTags(),
                PostStatus.DELETED,
                existing.visibility(),
                existing.likeCount(),
                existing.replyCount(),
                existing.hashtags(),
                existing.allowComments(),
                existing.createdAt(),
                now,
                now
        );

        Post saved = postRepository.save(deleted);
        return toResult(saved);
    }

    public String successMessage() {
        return "Xoa bai viet thanh cong.";
    }

    private void validateActor(DeletePostCommand command) {
        if (command.actorId() == null) {
            throw new AppException(ErrorCode.UNAUTHORIZED, "Authentication required");
        }
        userProjectionRepository.findByUserId(command.actorId()).ifPresent(user -> {
            if (user.isActionForbidden()) {
                throw new AppException(ErrorCode.ACCOUNT_SUSPENDED,
                        ErrorCode.ACCOUNT_SUSPENDED.defaultMessage());
            }
        });
    }

    private boolean canDelete(Post post, DeletePostCommand command) {
        if (post.authorId().equals(command.actorId().toString())) {
            return true;
        }
        if (command.actorRoles() == null) {
            return false;
        }
        return command.actorRoles().stream()
                .map(role -> role.toUpperCase(Locale.ROOT))
                .anyMatch(MODERATION_ROLES::contains);
    }

    private DeletePostResult toResult(Post post) {
        return new DeletePostResult(
                post.id(),
                post.status().name(),
                post.deletedAt() != null ? post.deletedAt().toString() : null,
                post.updatedAt() != null ? post.updatedAt().toString() : null
        );
    }
}
