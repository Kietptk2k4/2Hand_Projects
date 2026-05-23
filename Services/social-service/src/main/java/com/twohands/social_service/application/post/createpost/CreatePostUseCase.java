package com.twohands.social_service.application.post.createpost;

import com.twohands.social_service.application.post.common.ProductTagValidationItem;
import com.twohands.social_service.application.post.common.ProductTagValidator;
import com.twohands.social_service.domain.post.MediaItem;
import com.twohands.social_service.domain.post.Post;
import com.twohands.social_service.domain.post.PostRepository;
import com.twohands.social_service.domain.post.PostStatus;
import com.twohands.social_service.domain.post.PostVisibility;
import com.twohands.social_service.domain.post.ProductTag;
import com.twohands.social_service.application.user.common.UserWriteGuard;
import com.twohands.social_service.exception.AppException;
import com.twohands.social_service.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class CreatePostUseCase {

    private static final int MAX_CAPTION_LENGTH = 2000;
    private static final int MAX_MEDIA_ITEMS = 10;
    private static final int MAX_HASHTAGS = 30;
    private static final int MAX_HASHTAG_LENGTH = 100;

    private final PostRepository postRepository;
    private final UserWriteGuard userWriteGuard;
    private final ProductTagValidator productTagValidator;

    public CreatePostUseCase(
            PostRepository postRepository,
            UserWriteGuard userWriteGuard,
            ProductTagValidator productTagValidator) {
        this.postRepository = postRepository;
        this.userWriteGuard = userWriteGuard;
        this.productTagValidator = productTagValidator;
    }

    @Transactional
    public CreatePostResult execute(CreatePostCommand command) {
        userWriteGuard.assertCanWrite(command.authorId());
        validatePayload(command);

        PostVisibility visibility = parseVisibility(command.visibility());
        PostStatus status = command.publish() ? PostStatus.ACTIVE : PostStatus.DRAFT;
        Instant now = Instant.now();

        List<MediaItem> media = toMediaItems(command.media());
        List<ProductTag> productTags = toProductTags(command.productTags());

        Post post = new Post(
                null,
                command.authorId().toString(),
                command.caption(),
                media,
                productTags,
                status,
                visibility,
                0L,
                0L,
                command.hashtags() != null ? command.hashtags() : List.of(),
                command.allowComments(),
                now,
                now,
                null
        );

        Post saved = postRepository.save(post);
        return toResult(saved);
    }

    public String successMessage() {
        return "Tao bai viet thanh cong.";
    }

    private void validatePayload(CreatePostCommand command) {
        if (command.visibility() == null || command.visibility().isBlank()) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "Validation failed",
                    "visibility", "Visibility khong duoc de trong.");
        }
        if (!PostVisibility.PUBLIC.name().equals(command.visibility())
                && !PostVisibility.FOLLOWERS.name().equals(command.visibility())) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "Validation failed",
                    "visibility", "Visibility chi chap nhan PUBLIC hoac FOLLOWERS.");
        }
        if (command.caption() != null && command.caption().length() > MAX_CAPTION_LENGTH) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "Validation failed",
                    "caption", "Caption khong duoc vuot qua " + MAX_CAPTION_LENGTH + " ky tu.");
        }
        if (command.media() != null) {
            if (command.media().size() > MAX_MEDIA_ITEMS) {
                throw new AppException(ErrorCode.VALIDATION_ERROR, "Validation failed",
                        "media", "Khong duoc upload qua " + MAX_MEDIA_ITEMS + " media items.");
            }
            for (CreatePostCommand.MediaItemCommand item : command.media()) {
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
        if (command.hashtags() != null) {
            if (command.hashtags().size() > MAX_HASHTAGS) {
                throw new AppException(ErrorCode.VALIDATION_ERROR, "Validation failed",
                        "hashtags", "Khong duoc co qua " + MAX_HASHTAGS + " hashtags.");
            }
            for (String tag : command.hashtags()) {
                if (tag.length() > MAX_HASHTAG_LENGTH) {
                    throw new AppException(ErrorCode.VALIDATION_ERROR, "Validation failed",
                            "hashtags[]", "Moi hashtag khong duoc vuot qua " + MAX_HASHTAG_LENGTH + " ky tu.");
                }
            }
        }
        if (command.productTags() != null) {
            productTagValidator.validate(command.productTags().stream()
                    .map(pt -> new ProductTagValidationItem(pt.productId(), pt.price()))
                    .toList());
        }
    }

    private PostVisibility parseVisibility(String visibility) {
        return PostVisibility.valueOf(visibility);
    }

    private List<MediaItem> toMediaItems(List<CreatePostCommand.MediaItemCommand> items) {
        if (items == null) return List.of();
        return items.stream().map(i -> new MediaItem(i.url(), i.type())).toList();
    }

    private List<ProductTag> toProductTags(List<CreatePostCommand.ProductTagCommand> items) {
        if (items == null) return List.of();
        return items.stream().map(i -> new ProductTag(i.productId(), i.price())).toList();
    }

    private CreatePostResult toResult(Post post) {
        List<CreatePostResult.MediaItemData> media = post.media().stream()
                .map(m -> new CreatePostResult.MediaItemData(m.url(), m.type()))
                .toList();
        List<CreatePostResult.ProductTagData> productTags = post.productTags().stream()
                .map(pt -> new CreatePostResult.ProductTagData(pt.productId(), pt.price()))
                .toList();
        return new CreatePostResult(
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
