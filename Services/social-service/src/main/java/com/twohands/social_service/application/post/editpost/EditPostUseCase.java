package com.twohands.social_service.application.post.editpost;

import com.twohands.social_service.application.post.common.ProductTagValidationItem;
import com.twohands.social_service.application.post.common.ProductTagValidator;
import com.twohands.social_service.domain.post.MediaItem;
import com.twohands.social_service.domain.post.Post;
import com.twohands.social_service.domain.post.PostRepository;
import com.twohands.social_service.domain.post.PostStatus;
import com.twohands.social_service.domain.post.PostVisibility;
import com.twohands.social_service.domain.post.ProductTag;
import com.twohands.social_service.domain.user.UserProjectionRepository;
import com.twohands.social_service.exception.AppException;
import com.twohands.social_service.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

@Service
public class EditPostUseCase {

    private static final int MAX_CAPTION_LENGTH = 2000;
    private static final int MAX_MEDIA_ITEMS = 10;
    private static final int MAX_HASHTAGS = 30;
    private static final int MAX_HASHTAG_LENGTH = 100;
    private static final Pattern UNSAFE_CAPTION_PATTERN = Pattern.compile(
            "(?i)<\\s*script|javascript\\s*:|on\\w+\\s*="
    );

    private final PostRepository postRepository;
    private final UserProjectionRepository userProjectionRepository;
    private final ProductTagValidator productTagValidator;

    public EditPostUseCase(
            PostRepository postRepository,
            UserProjectionRepository userProjectionRepository,
            ProductTagValidator productTagValidator) {
        this.postRepository = postRepository;
        this.userProjectionRepository = userProjectionRepository;
        this.productTagValidator = productTagValidator;
    }

    @Transactional
    public EditPostResult execute(EditPostCommand command) {
        validateEditor(command);
        Post existing = postRepository.findById(command.postId())
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Bai viet khong ton tai."));

        if (existing.status() == PostStatus.DELETED) {
            throw new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Bai viet da bi xoa, khong the chinh sua.");
        }
        if (!existing.authorId().equals(command.editorId().toString())) {
            throw new AppException(ErrorCode.FORBIDDEN, "Ban khong co quyen chinh sua bai viet nay.");
        }

        validatePayload(command);

        Instant now = Instant.now();
        Post updated = new Post(
                existing.id(),
                existing.authorId(),
                resolveCaption(command, existing),
                resolveMedia(command, existing),
                resolveProductTags(command, existing),
                existing.status(),
                resolveVisibility(command, existing),
                existing.likeCount(),
                existing.replyCount(),
                resolveHashtags(command, existing),
                resolveAllowComments(command, existing),
                existing.createdAt(),
                now,
                existing.deletedAt()
        );

        Post saved = postRepository.save(updated);
        return toResult(saved);
    }

    public String successMessage() {
        return "Cap nhat bai viet thanh cong.";
    }

    private void validateEditor(EditPostCommand command) {
        if (command.editorId() == null) {
            throw new AppException(ErrorCode.UNAUTHORIZED, "Authentication required");
        }
        userProjectionRepository.findByUserId(command.editorId()).ifPresent(user -> {
            if (user.isActionForbidden()) {
                throw new AppException(ErrorCode.ACCOUNT_SUSPENDED,
                        ErrorCode.ACCOUNT_SUSPENDED.defaultMessage());
            }
        });
    }

    private void validatePayload(EditPostCommand command) {
        command.caption().ifPresent(caption -> {
            if (caption != null && caption.length() > MAX_CAPTION_LENGTH) {
                throw new AppException(ErrorCode.VALIDATION_ERROR, "Validation failed",
                        "caption", "Caption khong duoc vuot qua " + MAX_CAPTION_LENGTH + " ky tu.");
            }
            if (caption != null && UNSAFE_CAPTION_PATTERN.matcher(caption).find()) {
                throw new AppException(ErrorCode.VALIDATION_ERROR, "Validation failed",
                        "caption", "Caption chua noi dung khong hop le.");
            }
        });
        command.visibility().ifPresent(visibility -> {
            if (visibility == null || visibility.isBlank()) {
                throw new AppException(ErrorCode.VALIDATION_ERROR, "Validation failed",
                        "visibility", "Visibility khong duoc de trong.");
            }
            String normalized = visibility.toUpperCase(Locale.ROOT);
            if (!PostVisibility.PUBLIC.name().equals(normalized)
                    && !PostVisibility.FOLLOWERS.name().equals(normalized)) {
                throw new AppException(ErrorCode.VALIDATION_ERROR, "Validation failed",
                        "visibility", "Visibility chi chap nhan PUBLIC hoac FOLLOWERS.");
            }
        });
        command.media().ifPresent(media -> {
            if (media.size() > MAX_MEDIA_ITEMS) {
                throw new AppException(ErrorCode.VALIDATION_ERROR, "Validation failed",
                        "media", "Khong duoc upload qua " + MAX_MEDIA_ITEMS + " media items.");
            }
            for (EditPostCommand.MediaItemCommand item : media) {
                if (item.url() == null || item.url().isBlank()) {
                    throw new AppException(ErrorCode.VALIDATION_ERROR, "Validation failed",
                            "media[].url", "URL media khong duoc de trong.");
                }
                if (!"IMAGE".equals(item.type()) && !"VIDEO".equals(item.type())) {
                    throw new AppException(ErrorCode.VALIDATION_ERROR, "Validation failed",
                            "media[].type", "Media type chi chap nhan IMAGE hoac VIDEO.");
                }
            }
        });
        command.hashtags().ifPresent(hashtags -> {
            if (hashtags.size() > MAX_HASHTAGS) {
                throw new AppException(ErrorCode.VALIDATION_ERROR, "Validation failed",
                        "hashtags", "Khong duoc co qua " + MAX_HASHTAGS + " hashtags.");
            }
            for (String tag : hashtags) {
                if (tag != null && tag.length() > MAX_HASHTAG_LENGTH) {
                    throw new AppException(ErrorCode.VALIDATION_ERROR, "Validation failed",
                            "hashtags[]", "Moi hashtag khong duoc vuot qua " + MAX_HASHTAG_LENGTH + " ky tu.");
                }
            }
        });
        command.productTags().ifPresent(productTags -> productTagValidator.validate(productTags.stream()
                .map(pt -> new ProductTagValidationItem(pt.productId(), pt.price()))
                .toList()));
    }

    private String resolveCaption(EditPostCommand command, Post existing) {
        return command.caption().orElse(existing.caption());
    }

    private List<MediaItem> resolveMedia(EditPostCommand command, Post existing) {
        return command.media()
                .map(items -> items.stream().map(i -> new MediaItem(i.url(), i.type())).toList())
                .orElse(existing.media());
    }

    private List<ProductTag> resolveProductTags(EditPostCommand command, Post existing) {
        return command.productTags()
                .map(items -> items.stream().map(i -> new ProductTag(i.productId(), i.price())).toList())
                .orElse(existing.productTags());
    }

    private PostVisibility resolveVisibility(EditPostCommand command, Post existing) {
        return command.visibility()
                .map(v -> PostVisibility.valueOf(v.toUpperCase(Locale.ROOT)))
                .orElse(existing.visibility());
    }

    private List<String> resolveHashtags(EditPostCommand command, Post existing) {
        return command.hashtags().orElse(existing.hashtags());
    }

    private boolean resolveAllowComments(EditPostCommand command, Post existing) {
        return command.allowComments().orElse(existing.allowComments());
    }

    private EditPostResult toResult(Post post) {
        List<EditPostResult.MediaItemData> media = post.media().stream()
                .map(m -> new EditPostResult.MediaItemData(m.url(), m.type()))
                .toList();
        List<EditPostResult.ProductTagData> productTags = post.productTags().stream()
                .map(pt -> new EditPostResult.ProductTagData(pt.productId(), pt.price()))
                .toList();
        return new EditPostResult(
                post.id(),
                post.authorId(),
                post.caption(),
                media,
                productTags,
                post.status().name(),
                post.visibility().name(),
                post.allowComments(),
                post.hashtags(),
                post.createdAt() != null ? post.createdAt().toString() : null,
                post.updatedAt() != null ? post.updatedAt().toString() : null
        );
    }
}
