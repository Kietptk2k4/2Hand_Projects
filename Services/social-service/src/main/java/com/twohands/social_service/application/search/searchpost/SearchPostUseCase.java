package com.twohands.social_service.application.search.searchpost;

import com.twohands.social_service.domain.follow.FollowRepository;
import com.twohands.social_service.domain.post.MediaItem;
import com.twohands.social_service.domain.post.PageResult;
import com.twohands.social_service.domain.post.Post;
import com.twohands.social_service.domain.post.PostRepository;
import com.twohands.social_service.domain.post.PostSearchQuery;
import com.twohands.social_service.domain.search.SearchHistoryRepository;
import com.twohands.social_service.exception.AppException;
import com.twohands.social_service.exception.ErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
public class SearchPostUseCase {

    private static final Logger log = LoggerFactory.getLogger(SearchPostUseCase.class);
    private static final int MIN_PAGE = 0;
    private static final int MIN_SIZE = 1;
    private static final int MAX_SIZE = 50;
    private static final int MAX_KEYWORD_LENGTH = 255;

    private final PostRepository postRepository;
    private final FollowRepository followRepository;
    private final SearchHistoryRepository searchHistoryRepository;

    public SearchPostUseCase(
            PostRepository postRepository,
            FollowRepository followRepository,
            SearchHistoryRepository searchHistoryRepository
    ) {
        this.postRepository = postRepository;
        this.followRepository = followRepository;
        this.searchHistoryRepository = searchHistoryRepository;
    }

    public SearchPostResult execute(SearchPostCommand command) {
        if (command.userId() == null) {
            throw new AppException(ErrorCode.UNAUTHORIZED, "Authentication required");
        }

        String keyword = normalizeKeyword(command.keyword());
        validatePagination(command.page(), command.size());

        List<String> followeeAuthorIds = followRepository.findAcceptedFolloweeIds(command.userId()).stream()
                .map(UUID::toString)
                .toList();

        String keywordPattern = Pattern.quote(keyword);
        PageResult<Post> searchPage = postRepository.searchPosts(
                new PostSearchQuery(keywordPattern, command.page(), command.size()),
                followeeAuthorIds
        );

        List<SearchPostResult.SearchPostItem> items = searchPage.items().stream()
                .map(this::toItem)
                .toList();

        SearchPostResult result = SearchPostResult.from(keyword, new PageResult<>(
                items,
                searchPage.page(),
                searchPage.size(),
                searchPage.totalElements(),
                searchPage.totalPages(),
                searchPage.hasNext()
        ));

        saveSearchHistoryBestEffort(command.userId(), keyword);
        return result;
    }

    public String successMessage() {
        return "Tim kiem bai viet thanh cong.";
    }

    private void saveSearchHistoryBestEffort(UUID userId, String keyword) {
        try {
            searchHistoryRepository.saveOrRefresh(userId, keyword);
        } catch (Exception ex) {
            log.warn("Failed to save search history for userId={}", userId, ex);
        }
    }

    private String normalizeKeyword(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            throw new AppException(ErrorCode.BAD_REQUEST, "Tu khoa tim kiem khong hop le.");
        }
        String trimmed = keyword.trim();
        if (trimmed.length() > MAX_KEYWORD_LENGTH) {
            throw new AppException(ErrorCode.BAD_REQUEST, "Tu khoa tim kiem vuot qua gioi han cho phep.");
        }
        return trimmed;
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

    private SearchPostResult.SearchPostItem toItem(Post post) {
        List<SearchPostResult.MediaItemData> media = post.media().stream()
                .map(this::toMedia)
                .toList();
        return new SearchPostResult.SearchPostItem(
                post.id(),
                post.authorId(),
                post.caption(),
                media,
                post.visibility().name(),
                post.likeCount(),
                post.replyCount(),
                post.hashtags(),
                post.allowComments(),
                post.createdAt().toString(),
                post.updatedAt().toString()
        );
    }

    private SearchPostResult.MediaItemData toMedia(MediaItem mediaItem) {
        return new SearchPostResult.MediaItemData(mediaItem.url(), mediaItem.type());
    }
}
