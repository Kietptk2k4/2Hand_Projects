package com.twohands.social_service.application.feed.viewglobalfeed;

import com.twohands.social_service.domain.post.FeedQuery;
import com.twohands.social_service.domain.post.MediaItem;
import com.twohands.social_service.domain.post.PageResult;
import com.twohands.social_service.domain.post.Post;
import com.twohands.social_service.domain.post.PostLikeRepository;
import com.twohands.social_service.domain.post.PostRepository;
import com.twohands.social_service.exception.AppException;
import com.twohands.social_service.exception.ErrorCode;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class ViewGlobalFeedUseCase {

    private static final int MIN_PAGE = 0;
    private static final int MIN_SIZE = 1;
    private static final int MAX_SIZE = 50;
    private static final String SUCCESS_MESSAGE = "Lay global feed thanh cong.";

    private final PostRepository postRepository;
    private final PostLikeRepository postLikeRepository;

    public ViewGlobalFeedUseCase(PostRepository postRepository, PostLikeRepository postLikeRepository) {
        this.postRepository = postRepository;
        this.postLikeRepository = postLikeRepository;
    }

    public ViewGlobalFeedResult execute(UUID userId, int page, int size) {
        if (userId == null) {
            throw new AppException(ErrorCode.UNAUTHORIZED, "Authentication required");
        }
        validatePagination(page, size);

        PageResult<Post> feedPage = postRepository.findGlobalFeed(new FeedQuery(page, size));
        Set<String> likedPostIds = postLikeRepository.findLikedPostIdsByUserIdAndPostIds(
                userId,
                feedPage.items().stream().map(Post::id).toList()
        );
        List<ViewGlobalFeedResult.FeedPostItem> items = feedPage.items().stream()
                .map(post -> toItem(post, likedPostIds.contains(post.id())))
                .toList();

        return ViewGlobalFeedResult.from(new PageResult<>(
                items,
                feedPage.page(),
                feedPage.size(),
                feedPage.totalElements(),
                feedPage.totalPages(),
                feedPage.hasNext()
        ));
    }

    public String successMessage() {
        return SUCCESS_MESSAGE;
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

    private ViewGlobalFeedResult.FeedPostItem toItem(Post post, boolean likedByMe) {
        List<ViewGlobalFeedResult.MediaItemData> media = post.media()
                .stream()
                .map(this::toMedia)
                .toList();
        return new ViewGlobalFeedResult.FeedPostItem(
                post.id(),
                post.authorId(),
                post.caption(),
                media,
                post.visibility().name(),
                post.likeCount(),
                post.replyCount(),
                likedByMe,
                post.hashtags(),
                post.allowComments(),
                post.createdAt().toString(),
                post.updatedAt().toString()
        );
    }

    private ViewGlobalFeedResult.MediaItemData toMedia(MediaItem mediaItem) {
        return new ViewGlobalFeedResult.MediaItemData(mediaItem.url(), mediaItem.type());
    }
}
