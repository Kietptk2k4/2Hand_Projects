package com.twohands.social_service.application.admin.viewpostdetailformoderation;

import com.twohands.social_service.application.admin.common.AdminModerationAccessPolicy;
import com.twohands.social_service.application.admin.common.AdminModerationAuthorResolver;
import com.twohands.social_service.application.admin.common.PostMediaThumbnailResolver;
import com.twohands.social_service.application.post.common.PostIdValidator;
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
public class ViewPostDetailForModerationUseCase {

    private static final String SUCCESS_MESSAGE = "Lay chi tiet bai viet kiem duyet thanh cong.";

    private final PostRepository postRepository;
    private final PostIdValidator postIdValidator;
    private final AdminModerationAuthorResolver authorResolver;

    public ViewPostDetailForModerationUseCase(
            PostRepository postRepository,
            PostIdValidator postIdValidator,
            AdminModerationAuthorResolver authorResolver
    ) {
        this.postRepository = postRepository;
        this.postIdValidator = postIdValidator;
        this.authorResolver = authorResolver;
    }

    @Transactional(readOnly = true)
    public ViewPostDetailForModerationResult execute(ViewPostDetailForModerationCommand command) {
        AdminModerationAccessPolicy.ensureCanViewPostList(command.actor());
        postIdValidator.validate(command.postId());

        Post post = postRepository.findById(command.postId())
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Bai viet khong ton tai."));

        AdminModerationAuthorResolver.AuthorSummary author = authorResolver.resolveAuthor(post.authorId());
        List<MediaItem> media = post.media() == null ? List.of() : post.media();

        return new ViewPostDetailForModerationResult(
                post.id(),
                new ViewPostDetailForModerationResult.AuthorSummary(
                        author.userId() != null ? author.userId() : post.authorId(),
                        author.displayName(),
                        author.avatarUrl()
                ),
                post.caption(),
                media.stream().map(this::toMedia).toList(),
                PostMediaThumbnailResolver.resolveThumbnailUrl(media),
                PostMediaThumbnailResolver.countMedia(media),
                post.status().name(),
                resolveModerationStatus(post.moderationStatusOrDefault()),
                post.moderationReason(),
                post.lastModerationLogId(),
                post.visibility().name(),
                post.likeCount(),
                post.replyCount(),
                post.hashtags() == null ? List.of() : post.hashtags(),
                post.allowComments(),
                post.createdAt(),
                post.updatedAt()
        );
    }

    public String successMessage() {
        return SUCCESS_MESSAGE;
    }

    private ViewPostDetailForModerationResult.MediaItemData toMedia(MediaItem mediaItem) {
        return new ViewPostDetailForModerationResult.MediaItemData(
                mediaItem.url(),
                mediaItem.type(),
                mediaItem.width(),
                mediaItem.height()
        );
    }

    private String resolveModerationStatus(PostModerationStatus moderationStatus) {
        return moderationStatus == null ? PostModerationStatus.NONE.name() : moderationStatus.name();
    }
}
