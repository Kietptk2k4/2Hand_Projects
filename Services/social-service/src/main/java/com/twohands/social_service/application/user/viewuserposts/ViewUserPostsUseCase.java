package com.twohands.social_service.application.user.viewuserposts;

import com.twohands.social_service.domain.follow.FollowRepository;
import com.twohands.social_service.domain.follow.FollowStatus;
import com.twohands.social_service.domain.post.AuthorPostsQuery;
import com.twohands.social_service.domain.post.AuthorPostsScope;
import com.twohands.social_service.domain.post.MediaItem;
import com.twohands.social_service.domain.post.PageResult;
import com.twohands.social_service.domain.post.Post;
import com.twohands.social_service.domain.post.PostRepository;
import com.twohands.social_service.domain.user.UserProjection;
import com.twohands.social_service.domain.user.UserProjectionRepository;
import com.twohands.social_service.exception.AppException;
import com.twohands.social_service.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ViewUserPostsUseCase {

    private static final int MIN_PAGE = 0;
    private static final int MIN_SIZE = 1;
    private static final int MAX_SIZE = 50;
    private static final String SUCCESS_MESSAGE = "Lay danh sach bai viet thanh cong.";

    private final PostRepository postRepository;
    private final UserProjectionRepository userProjectionRepository;
    private final FollowRepository followRepository;

    public ViewUserPostsUseCase(
            PostRepository postRepository,
            UserProjectionRepository userProjectionRepository,
            FollowRepository followRepository
    ) {
        this.postRepository = postRepository;
        this.userProjectionRepository = userProjectionRepository;
        this.followRepository = followRepository;
    }

    @Transactional(readOnly = true)
    public ViewUserPostsResult execute(
            UUID viewerId,
            UUID targetUserId,
            int page,
            int size,
            String statusFilterParam
    ) {
        requireAuthenticated(viewerId);
        validatePagination(page, size);

        UserPostsStatusFilter statusFilter = UserPostsStatusFilter.fromQueryParam(statusFilterParam);
        if (statusFilter == null) {
            throw new AppException(
                    ErrorCode.INVALID_PAGINATION,
                    "Tham so pagination khong hop le.",
                    "status_filter",
                    "MUST_BE_PUBLISHED_OR_ALL"
            );
        }

        boolean isOwner = viewerId.equals(targetUserId);
        if (statusFilter == UserPostsStatusFilter.ALL && !isOwner) {
            throw new AppException(
                    ErrorCode.INVALID_PAGINATION,
                    "Tham so pagination khong hop le.",
                    "status_filter",
                    "ONLY_OWNER_CAN_USE_ALL"
            );
        }

        UserProjection target = userProjectionRepository.findByUserId(targetUserId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Nguoi dung khong ton tai."));

        if (target.isDeleted()) {
            throw new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Nguoi dung khong ton tai hoac da bi xoa.");
        }

        boolean acceptedFollow = isAcceptedFollow(viewerId, targetUserId, isOwner);
        if (!isOwner && target.isPrivateProfile() && !acceptedFollow) {
            throw new AppException(
                    ErrorCode.FORBIDDEN,
                    "Tai khoan rieng tu. Ban can duoc chap nhan follow de xem bai viet."
            );
        }

        AuthorPostsScope scope = resolveScope(isOwner, statusFilter, acceptedFollow);
        PageResult<Post> postsPage = postRepository.findAuthorPosts(
                new AuthorPostsQuery(targetUserId.toString(), scope, page, size)
        );

        List<ViewUserPostsResult.UserPostItem> items = postsPage.items().stream()
                .map(this::toItem)
                .toList();

        return ViewUserPostsResult.from(new PageResult<>(
                items,
                postsPage.page(),
                postsPage.size(),
                postsPage.totalElements(),
                postsPage.totalPages(),
                postsPage.hasNext()
        ));
    }

    public String successMessage() {
        return SUCCESS_MESSAGE;
    }

    private void requireAuthenticated(UUID viewerId) {
        if (viewerId == null) {
            throw new AppException(ErrorCode.UNAUTHORIZED, "Authentication required");
        }
    }

    private void validatePagination(int page, int size) {
        if (page < MIN_PAGE) {
            throw new AppException(
                    ErrorCode.INVALID_PAGINATION,
                    "Tham so pagination khong hop le.",
                    "page",
                    "MUST_BE_GREATER_THAN_OR_EQUAL_TO_0"
            );
        }
        if (size < MIN_SIZE || size > MAX_SIZE) {
            throw new AppException(
                    ErrorCode.INVALID_PAGINATION,
                    "Tham so pagination khong hop le.",
                    "size",
                    "MUST_BE_BETWEEN_1_AND_50"
            );
        }
    }

    private boolean isAcceptedFollow(UUID viewerId, UUID targetUserId, boolean isOwner) {
        if (isOwner) {
            return true;
        }
        return followRepository.findByFollowerIdAndFolloweeId(viewerId, targetUserId)
                .filter(follow -> follow.status() == FollowStatus.ACCEPTED)
                .isPresent();
    }

    private AuthorPostsScope resolveScope(
            boolean isOwner,
            UserPostsStatusFilter statusFilter,
            boolean acceptedFollow
    ) {
        if (isOwner) {
            return statusFilter == UserPostsStatusFilter.ALL
                    ? AuthorPostsScope.OWNER_ALL
                    : AuthorPostsScope.OWNER_PUBLISHED;
        }
        if (acceptedFollow) {
            return AuthorPostsScope.VIEWER_AS_FOLLOWER;
        }
        return AuthorPostsScope.VIEWER_PUBLIC_ONLY;
    }

    private ViewUserPostsResult.UserPostItem toItem(Post post) {
        List<ViewUserPostsResult.MediaItemData> media = post.media().stream()
                .map(this::toMedia)
                .toList();
        return new ViewUserPostsResult.UserPostItem(
                post.id(),
                post.caption(),
                media,
                post.visibility().name(),
                post.likeCount(),
                post.replyCount(),
                post.hashtags(),
                post.createdAt() != null ? post.createdAt().toString() : null
        );
    }

    private ViewUserPostsResult.MediaItemData toMedia(MediaItem mediaItem) {
        return new ViewUserPostsResult.MediaItemData(mediaItem.url(), mediaItem.type(), mediaItem.width(), mediaItem.height());
    }
}
