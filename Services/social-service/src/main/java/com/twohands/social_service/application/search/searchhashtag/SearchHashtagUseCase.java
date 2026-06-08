package com.twohands.social_service.application.search.searchhashtag;

import com.twohands.social_service.domain.follow.FollowRepository;
import com.twohands.social_service.domain.post.MediaItem;
import com.twohands.social_service.domain.post.PageResult;
import com.twohands.social_service.domain.post.Post;
import com.twohands.social_service.domain.post.PostHashtagSearchQuery;
import com.twohands.social_service.domain.post.PostRepository;
import com.twohands.social_service.exception.AppException;
import com.twohands.social_service.exception.ErrorCode;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
public class SearchHashtagUseCase {

    private static final int MIN_PAGE = 0;
    private static final int MIN_SIZE = 1;
    private static final int MAX_SIZE = 50;
    private static final int MAX_HASHTAG_LENGTH = 100;
    private static final Pattern HASHTAG_PATTERN = Pattern.compile("^[a-zA-Z0-9_]+$");

    private final PostRepository postRepository;
    private final FollowRepository followRepository;

    public SearchHashtagUseCase(PostRepository postRepository, FollowRepository followRepository) {
        this.postRepository = postRepository;
        this.followRepository = followRepository;
    }

    public SearchHashtagResult execute(SearchHashtagCommand command) {
        if (command.userId() == null) {
            throw new AppException(ErrorCode.UNAUTHORIZED, "Authentication required");
        }

        String hashtag = normalizeHashtag(command.hashtag());
        validatePagination(command.page(), command.size());

        List<String> followeeAuthorIds = followRepository.findAcceptedFolloweeIds(command.userId()).stream()
                .map(UUID::toString)
                .toList();

        PageResult<Post> searchPage = postRepository.searchPostsByHashtag(
                new PostHashtagSearchQuery(buildHashtagVariants(hashtag), command.page(), command.size()),
                followeeAuthorIds
        );

        List<SearchHashtagResult.SearchHashtagPostItem> items = searchPage.items().stream()
                .map(this::toItem)
                .toList();

        return SearchHashtagResult.from(hashtag, new PageResult<>(
                items,
                searchPage.page(),
                searchPage.size(),
                searchPage.totalElements(),
                searchPage.totalPages(),
                searchPage.hasNext()
        ));
    }

    public String successMessage() {
        return "Tim kiem hashtag thanh cong.";
    }

    private String normalizeHashtag(String rawHashtag) {
        if (rawHashtag == null || rawHashtag.isBlank()) {
            throw new AppException(ErrorCode.BAD_REQUEST, "Hashtag khong hop le.");
        }
        String trimmed = rawHashtag.trim();
        if (trimmed.startsWith("#")) {
            trimmed = trimmed.substring(1).trim();
        }
        if (trimmed.isBlank()) {
            throw new AppException(ErrorCode.BAD_REQUEST, "Hashtag khong hop le.");
        }
        if (trimmed.length() > MAX_HASHTAG_LENGTH) {
            throw new AppException(ErrorCode.BAD_REQUEST, "Hashtag vuot qua gioi han cho phep.");
        }
        if (!HASHTAG_PATTERN.matcher(trimmed).matches()) {
            throw new AppException(ErrorCode.BAD_REQUEST, "Hashtag chi chap nhan chu cai, so va dau gach duoi.");
        }
        return trimmed;
    }

    private List<String> buildHashtagVariants(String hashtag) {
        List<String> variants = new ArrayList<>();
        variants.add(hashtag);
        String withHash = "#" + hashtag;
        if (!variants.contains(withHash)) {
            variants.add(withHash);
        }
        return variants;
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

    private SearchHashtagResult.SearchHashtagPostItem toItem(Post post) {
        List<SearchHashtagResult.MediaItemData> media = post.media().stream()
                .map(this::toMedia)
                .toList();
        return new SearchHashtagResult.SearchHashtagPostItem(
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

    private SearchHashtagResult.MediaItemData toMedia(MediaItem mediaItem) {
        return new SearchHashtagResult.MediaItemData(mediaItem.url(), mediaItem.type(), mediaItem.width(), mediaItem.height());
    }
}
