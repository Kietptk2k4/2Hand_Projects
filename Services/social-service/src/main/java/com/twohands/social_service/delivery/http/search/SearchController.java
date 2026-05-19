package com.twohands.social_service.delivery.http.search;

import com.twohands.social_service.application.search.searchhashtag.SearchHashtagCommand;
import com.twohands.social_service.application.search.searchhashtag.SearchHashtagResult;
import com.twohands.social_service.application.search.searchhashtag.SearchHashtagUseCase;
import com.twohands.social_service.application.search.searchpost.SearchPostCommand;
import com.twohands.social_service.application.search.searchpost.SearchPostResult;
import com.twohands.social_service.application.search.searchpost.SearchPostUseCase;
import com.twohands.social_service.common.dto.ApiResponse;
import com.twohands.social_service.delivery.http.search.response.SearchHashtagResponse;
import com.twohands.social_service.delivery.http.search.response.SearchPostResponse;
import com.twohands.social_service.security.AuthenticatedUser;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/social/search")
public class SearchController {

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 20;

    private final SearchPostUseCase searchPostUseCase;
    private final SearchHashtagUseCase searchHashtagUseCase;

    public SearchController(SearchPostUseCase searchPostUseCase, SearchHashtagUseCase searchHashtagUseCase) {
        this.searchPostUseCase = searchPostUseCase;
        this.searchHashtagUseCase = searchHashtagUseCase;
    }

    @GetMapping("/hashtags/{hashtag}")
    public ResponseEntity<ApiResponse<SearchHashtagResponse>> searchHashtag(
            @PathVariable String hashtag,
            @RequestParam(name = "page", defaultValue = "" + DEFAULT_PAGE) int page,
            @RequestParam(name = "size", defaultValue = "" + DEFAULT_SIZE) int size,
            Authentication authentication
    ) {
        UUID userId = resolveUserId(authentication);
        SearchHashtagResult result = searchHashtagUseCase.execute(
                new SearchHashtagCommand(userId, hashtag, page, size)
        );

        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK.value(),
                searchHashtagUseCase.successMessage(),
                toHashtagResponse(result)
        ));
    }

    @GetMapping("/posts")
    public ResponseEntity<ApiResponse<SearchPostResponse>> searchPosts(
            @RequestParam("q") String keyword,
            @RequestParam(name = "page", defaultValue = "" + DEFAULT_PAGE) int page,
            @RequestParam(name = "size", defaultValue = "" + DEFAULT_SIZE) int size,
            Authentication authentication
    ) {
        UUID userId = resolveUserId(authentication);
        SearchPostResult result = searchPostUseCase.execute(new SearchPostCommand(userId, keyword, page, size));

        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK.value(),
                searchPostUseCase.successMessage(),
                toResponse(result)
        ));
    }

    private UUID resolveUserId(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedUser principal)) {
            return null;
        }
        return principal.userId();
    }

    private SearchPostResponse toResponse(SearchPostResult result) {
        List<SearchPostResponse.PostItemResponse> items = result.items().stream()
                .map(item -> new SearchPostResponse.PostItemResponse(
                        item.postId(),
                        item.authorId(),
                        item.caption(),
                        item.media().stream()
                                .map(m -> new SearchPostResponse.MediaItemResponse(m.url(), m.type()))
                                .toList(),
                        item.visibility(),
                        item.likeCount(),
                        item.replyCount(),
                        item.hashtags(),
                        item.allowComments(),
                        item.createdAt(),
                        item.updatedAt()
                ))
                .toList();
        return new SearchPostResponse(
                result.keyword(),
                items,
                new SearchPostResponse.PageMetaResponse(
                        result.meta().page(),
                        result.meta().size(),
                        result.meta().totalElements(),
                        result.meta().totalPages(),
                        result.meta().hasNext()
                )
        );
    }

    private SearchHashtagResponse toHashtagResponse(SearchHashtagResult result) {
        List<SearchHashtagResponse.PostItemResponse> items = result.items().stream()
                .map(item -> new SearchHashtagResponse.PostItemResponse(
                        item.postId(),
                        item.authorId(),
                        item.caption(),
                        item.media().stream()
                                .map(m -> new SearchHashtagResponse.MediaItemResponse(m.url(), m.type()))
                                .toList(),
                        item.visibility(),
                        item.likeCount(),
                        item.replyCount(),
                        item.hashtags(),
                        item.allowComments(),
                        item.createdAt(),
                        item.updatedAt()
                ))
                .toList();
        return new SearchHashtagResponse(
                result.hashtag(),
                items,
                new SearchHashtagResponse.PageMetaResponse(
                        result.meta().page(),
                        result.meta().size(),
                        result.meta().totalElements(),
                        result.meta().totalPages(),
                        result.meta().hasNext()
                )
        );
    }
}
