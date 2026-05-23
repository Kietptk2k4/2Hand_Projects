package com.twohands.social_service.application.post.viewsavedposts;

import com.twohands.social_service.application.post.common.PostViewAccessPolicy;
import com.twohands.social_service.domain.follow.FollowRepository;
import com.twohands.social_service.domain.post.MediaItem;
import com.twohands.social_service.domain.post.PageResult;
import com.twohands.social_service.domain.post.Post;
import com.twohands.social_service.domain.post.PostRepository;
import com.twohands.social_service.domain.post.PostSaveEntry;
import com.twohands.social_service.domain.post.PostSaveRepository;
import com.twohands.social_service.exception.AppException;
import com.twohands.social_service.exception.ErrorCode;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class ViewSavedPostsUseCase {

    private static final int MIN_PAGE = 0;
    private static final int MIN_SIZE = 1;
    private static final int MAX_SIZE = 50;
    private static final String SUCCESS_MESSAGE = "Lay danh sach bai da luu thanh cong.";

    private final PostSaveRepository postSaveRepository;
    private final PostRepository postRepository;
    private final FollowRepository followRepository;
    private final PostViewAccessPolicy postViewAccessPolicy;

    public ViewSavedPostsUseCase(
            PostSaveRepository postSaveRepository,
            PostRepository postRepository,
            FollowRepository followRepository,
            PostViewAccessPolicy postViewAccessPolicy
    ) {
        this.postSaveRepository = postSaveRepository;
        this.postRepository = postRepository;
        this.followRepository = followRepository;
        this.postViewAccessPolicy = postViewAccessPolicy;
    }

    public ViewSavedPostsResult execute(UUID userId, int page, int size) {
        requireAuthenticated(userId);
        validatePagination(page, size);

        PageResult<PostSaveEntry> savesPage = postSaveRepository.findByUserId(userId, page, size);
        if (savesPage.items().isEmpty()) {
            return ViewSavedPostsResult.from(new PageResult<>(
                    List.of(),
                    savesPage.page(),
                    savesPage.size(),
                    savesPage.totalElements(),
                    savesPage.totalPages(),
                    savesPage.hasNext()
            ));
        }

        List<String> postIds = savesPage.items().stream()
                .map(PostSaveEntry::postId)
                .toList();
        List<String> followeeAuthorIds = followRepository.findAcceptedFolloweeIds(userId).stream()
                .map(UUID::toString)
                .toList();

        Map<String, Post> postsById = new HashMap<>();
        for (Post post : postRepository.findByIds(postIds)) {
            postsById.put(post.id(), post);
        }

        List<ViewSavedPostsResult.SavedPostItem> items = savesPage.items().stream()
                .map(save -> {
                    Post post = postsById.get(save.postId());
                    if (!postViewAccessPolicy.canView(post, userId, followeeAuthorIds)) {
                        return null;
                    }
                    return toItem(post, save);
                })
                .filter(item -> item != null)
                .toList();

        return ViewSavedPostsResult.from(new PageResult<>(
                items,
                savesPage.page(),
                savesPage.size(),
                savesPage.totalElements(),
                savesPage.totalPages(),
                savesPage.hasNext()
        ));
    }

    public String successMessage() {
        return SUCCESS_MESSAGE;
    }

    private void requireAuthenticated(UUID userId) {
        if (userId == null) {
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

    private ViewSavedPostsResult.SavedPostItem toItem(Post post, PostSaveEntry save) {
        List<ViewSavedPostsResult.MediaItemData> media = post.media().stream()
                .map(this::toMedia)
                .toList();
        return new ViewSavedPostsResult.SavedPostItem(
                post.id(),
                post.authorId(),
                post.caption(),
                media,
                post.visibility().name(),
                post.likeCount(),
                post.replyCount(),
                post.hashtags(),
                post.allowComments(),
                save.savedAt().toString(),
                post.createdAt().toString(),
                post.updatedAt().toString()
        );
    }

    private ViewSavedPostsResult.MediaItemData toMedia(MediaItem mediaItem) {
        return new ViewSavedPostsResult.MediaItemData(mediaItem.url(), mediaItem.type());
    }
}
