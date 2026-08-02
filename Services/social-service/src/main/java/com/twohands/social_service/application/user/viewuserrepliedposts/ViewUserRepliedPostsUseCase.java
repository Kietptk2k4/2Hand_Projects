package com.twohands.social_service.application.user.viewuserrepliedposts;

import com.twohands.social_service.application.user.viewuserposts.ViewUserPostsResult;
import com.twohands.social_service.domain.follow.FollowRepository;
import com.twohands.social_service.domain.follow.FollowStatus;
import com.twohands.social_service.domain.post.PageResult;
import com.twohands.social_service.domain.user.UserProjection;
import com.twohands.social_service.domain.user.UserProjectionRepository;
import com.twohands.social_service.exception.AppException;
import com.twohands.social_service.exception.ErrorCode;
import com.twohands.social_service.infrastructure.persistence.mongo.document.CommentDocument;
import com.twohands.social_service.infrastructure.persistence.mongo.document.PostDocument;
import com.twohands.social_service.infrastructure.persistence.mongo.repository.MongoCommentRepository;
import com.twohands.social_service.infrastructure.persistence.mongo.repository.MongoPostRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
public class ViewUserRepliedPostsUseCase {

    private static final int MIN_PAGE = 0;
    private static final int MIN_SIZE = 1;
    private static final int MAX_SIZE = 50;

    private final MongoCommentRepository mongoCommentRepository;
    private final MongoPostRepository mongoPostRepository;
    private final UserProjectionRepository userProjectionRepository;
    private final FollowRepository followRepository;

    public ViewUserRepliedPostsUseCase(
            MongoCommentRepository mongoCommentRepository,
            MongoPostRepository mongoPostRepository,
            UserProjectionRepository userProjectionRepository,
            FollowRepository followRepository
    ) {
        this.mongoCommentRepository = mongoCommentRepository;
        this.mongoPostRepository = mongoPostRepository;
        this.userProjectionRepository = userProjectionRepository;
        this.followRepository = followRepository;
    }

    @Transactional(readOnly = true)
    public ViewUserPostsResult execute(UUID viewerId, UUID targetUserId, int page, int size) {
        if (viewerId == null) {
            throw new AppException(ErrorCode.UNAUTHORIZED, "Authentication required");
        }
        if (page < MIN_PAGE || size < MIN_SIZE || size > MAX_SIZE) {
            throw new AppException(ErrorCode.INVALID_PAGINATION, "Tham so pagination khong hop le.");
        }

        UserProjection target = userProjectionRepository.findByUserId(targetUserId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Nguoi dung khong ton tai."));

        if (target.isDeleted()) {
            throw new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Nguoi dung khong ton tai hoac da bi xoa.");
        }

        boolean isOwner = viewerId.equals(targetUserId);
        boolean acceptedFollow = isOwner || followRepository.findByFollowerIdAndFolloweeId(viewerId, targetUserId)
                .filter(follow -> follow.status() == FollowStatus.ACCEPTED)
                .isPresent();

        if (!isOwner && target.isPrivateProfile() && !acceptedFollow) {
            throw new AppException(ErrorCode.FORBIDDEN, "Tai khoan rieng tu.");
        }

        List<CommentDocument> userComments = mongoCommentRepository.findByAuthorIdAndStatus(targetUserId.toString(), "ACTIVE");
        List<String> postIds = userComments.stream()
                .map(CommentDocument::getPostId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        List<PostDocument> postDocs = postIds.isEmpty() ? List.of() : mongoPostRepository.findByIdIn(postIds);

        List<ViewUserPostsResult.UserPostItem> items = postDocs.stream()
                .filter(doc -> "ACTIVE".equalsIgnoreCase(doc.getStatus()))
                .filter(doc -> doc.getModerationStatus() == null || "NONE".equalsIgnoreCase(doc.getModerationStatus()))
                .filter(doc -> "PUBLIC".equalsIgnoreCase(doc.getVisibility()) || (acceptedFollow && "FOLLOWERS".equalsIgnoreCase(doc.getVisibility())))
                .map(this::toItem)
                .toList();

        int totalElements = items.size();
        int totalPages = totalElements == 0 ? 0 : (int) Math.ceil((double) totalElements / size);
        int start = Math.min(page * size, totalElements);
        int end = Math.min(start + size, totalElements);
        List<ViewUserPostsResult.UserPostItem> pagedItems = items.subList(start, end);

        return ViewUserPostsResult.from(new PageResult<>(
                pagedItems,
                page,
                size,
                totalElements,
                totalPages,
                end < totalElements
        ));
    }

    private ViewUserPostsResult.UserPostItem toItem(PostDocument doc) {
        List<ViewUserPostsResult.MediaItemData> media = doc.getMedia() != null
                ? doc.getMedia().stream().map(m -> new ViewUserPostsResult.MediaItemData(m.getUrl(), m.getType(), m.getWidth(), m.getHeight())).toList()
                : List.of();
        List<String> hashtags = doc.getHashtags() != null ? doc.getHashtags() : List.of();

        return new ViewUserPostsResult.UserPostItem(
                doc.getId(),
                doc.getAuthorId(),
                doc.getCaption() != null ? doc.getCaption() : "",
                media,
                doc.getVisibility() != null ? doc.getVisibility() : "PUBLIC",
                doc.getLikeCount(),
                doc.getReplyCount(),
                hashtags,
                doc.getCreatedAt() != null ? doc.getCreatedAt().toString() : null
        );
    }
}
