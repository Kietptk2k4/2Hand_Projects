package com.twohands.social_service.application.post.viewpostdetail;

import com.twohands.social_service.application.post.common.PostIdValidator;
import com.twohands.social_service.application.post.common.PostViewAccessPolicy;
import com.twohands.social_service.application.post.common.PostViewAccessPolicy.PostViewAccessOutcome;
import com.twohands.social_service.domain.follow.FollowRepository;
import com.twohands.social_service.domain.post.MediaItem;
import com.twohands.social_service.domain.post.Post;
import com.twohands.social_service.domain.post.PostLikeRepository;
import com.twohands.social_service.domain.post.PostRepository;
import com.twohands.social_service.domain.post.PostSaveRepository;
import com.twohands.social_service.application.post.common.ProductTagSnapshotData;
import com.twohands.social_service.domain.post.ProductTag;
import com.twohands.social_service.domain.user.UserProjection;
import com.twohands.social_service.domain.user.UserProjectionRepository;
import com.twohands.social_service.exception.AppException;
import com.twohands.social_service.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ViewPostDetailUseCase {

    private static final String SUCCESS_MESSAGE = "Lay chi tiet bai viet thanh cong.";

    private final PostRepository postRepository;
    private final PostLikeRepository postLikeRepository;
    private final PostSaveRepository postSaveRepository;
    private final FollowRepository followRepository;
    private final UserProjectionRepository userProjectionRepository;
    private final PostViewAccessPolicy postViewAccessPolicy;
    private final PostIdValidator postIdValidator;

    public ViewPostDetailUseCase(
            PostRepository postRepository,
            PostLikeRepository postLikeRepository,
            PostSaveRepository postSaveRepository,
            FollowRepository followRepository,
            UserProjectionRepository userProjectionRepository,
            PostViewAccessPolicy postViewAccessPolicy,
            PostIdValidator postIdValidator
    ) {
        this.postRepository = postRepository;
        this.postLikeRepository = postLikeRepository;
        this.postSaveRepository = postSaveRepository;
        this.followRepository = followRepository;
        this.userProjectionRepository = userProjectionRepository;
        this.postViewAccessPolicy = postViewAccessPolicy;
        this.postIdValidator = postIdValidator;
    }

    @Transactional(readOnly = true)
    public ViewPostDetailResult execute(UUID viewerId, String postId) {
        requireAuthenticated(viewerId);
        postIdValidator.validate(postId);

        Post post = postRepository.findById(postId).orElse(null);
        List<String> followeeAuthorIds = followRepository.findAcceptedFolloweeIds(viewerId).stream()
                .map(UUID::toString)
                .toList();

        PostViewAccessOutcome access = postViewAccessPolicy.evaluateViewAccess(post, viewerId, followeeAuthorIds);
        if (access == PostViewAccessOutcome.FORBIDDEN) {
            throw new AppException(ErrorCode.FORBIDDEN, "Ban khong co quyen xem bai viet nay.");
        }
        if (access != PostViewAccessOutcome.ALLOWED) {
            throw new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Bai viet khong ton tai.");
        }

        boolean likedByMe = postLikeRepository.existsByPostIdAndUserId(postId, viewerId);
        boolean savedByMe = postSaveRepository.existsByPostIdAndUserId(postId, viewerId);
        boolean isOwner = post.authorId().equals(viewerId.toString());

        return toResult(post, resolveAuthor(post.authorId()), likedByMe, savedByMe, isOwner);
    }

    public String successMessage() {
        return SUCCESS_MESSAGE;
    }

    private void requireAuthenticated(UUID viewerId) {
        if (viewerId == null) {
            throw new AppException(ErrorCode.UNAUTHORIZED, "Authentication required");
        }
    }

    private ViewPostDetailResult.AuthorSummary resolveAuthor(String authorId) {
        UUID authorUserId;
        try {
            authorUserId = UUID.fromString(authorId);
        } catch (IllegalArgumentException ex) {
            return new ViewPostDetailResult.AuthorSummary(authorId, "User", null);
        }

        return userProjectionRepository.findByUserId(authorUserId)
                .filter(projection -> !projection.isDeleted())
                .map(this::toAuthorSummary)
                .orElseGet(() -> new ViewPostDetailResult.AuthorSummary(authorId, "User", null));
    }

    private ViewPostDetailResult.AuthorSummary toAuthorSummary(UserProjection projection) {
        return new ViewPostDetailResult.AuthorSummary(
                projection.userId(),
                projection.displayName(),
                projection.avatarUrl()
        );
    }

    private ViewPostDetailResult toResult(
            Post post,
            ViewPostDetailResult.AuthorSummary author,
            boolean likedByMe,
            boolean savedByMe,
            boolean isOwner
    ) {
        List<ViewPostDetailResult.MediaItemData> media = post.media().stream()
                .map(this::toMedia)
                .toList();
        List<ProductTagSnapshotData> productTags = post.productTags().stream()
                .map(ProductTagSnapshotData::fromDomain)
                .toList();

        return new ViewPostDetailResult(
                post.id(),
                author,
                post.caption(),
                media,
                productTags,
                post.visibility().name(),
                post.status().name(),
                post.likeCount(),
                post.replyCount(),
                post.hashtags(),
                post.allowComments(),
                likedByMe,
                savedByMe,
                isOwner,
                post.createdAt().toString(),
                post.updatedAt().toString()
        );
    }

    private ViewPostDetailResult.MediaItemData toMedia(MediaItem mediaItem) {
        return new ViewPostDetailResult.MediaItemData(mediaItem.url(), mediaItem.type(), mediaItem.width(), mediaItem.height());
    }

}
