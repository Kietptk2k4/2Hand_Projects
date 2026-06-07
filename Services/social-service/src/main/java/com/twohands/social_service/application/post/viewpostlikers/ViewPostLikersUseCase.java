package com.twohands.social_service.application.post.viewpostlikers;

import com.twohands.social_service.application.post.common.PostIdValidator;
import com.twohands.social_service.application.post.common.PostViewAccessPolicy;
import com.twohands.social_service.application.post.common.PostViewAccessPolicy.PostViewAccessOutcome;
import com.twohands.social_service.application.reaction.common.LikeUserEnricher;
import com.twohands.social_service.application.reaction.common.ViewLikeUsersResult;
import com.twohands.social_service.domain.follow.FollowRepository;
import com.twohands.social_service.domain.post.PageResult;
import com.twohands.social_service.domain.post.Post;
import com.twohands.social_service.domain.post.PostLikeEntry;
import com.twohands.social_service.domain.post.PostLikeRepository;
import com.twohands.social_service.domain.post.PostRepository;
import com.twohands.social_service.exception.AppException;
import com.twohands.social_service.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ViewPostLikersUseCase {

    private static final int MIN_PAGE = 0;
    private static final int MIN_SIZE = 1;
    private static final int MAX_SIZE = 50;
    private static final String SUCCESS_MESSAGE = "Lay danh sach nguoi thich bai viet thanh cong.";

    private final PostRepository postRepository;
    private final PostLikeRepository postLikeRepository;
    private final FollowRepository followRepository;
    private final PostViewAccessPolicy postViewAccessPolicy;
    private final PostIdValidator postIdValidator;
    private final LikeUserEnricher likeUserEnricher;

    public ViewPostLikersUseCase(
            PostRepository postRepository,
            PostLikeRepository postLikeRepository,
            FollowRepository followRepository,
            PostViewAccessPolicy postViewAccessPolicy,
            PostIdValidator postIdValidator,
            LikeUserEnricher likeUserEnricher
    ) {
        this.postRepository = postRepository;
        this.postLikeRepository = postLikeRepository;
        this.followRepository = followRepository;
        this.postViewAccessPolicy = postViewAccessPolicy;
        this.postIdValidator = postIdValidator;
        this.likeUserEnricher = likeUserEnricher;
    }

    @Transactional(readOnly = true)
    public ViewLikeUsersResult execute(UUID viewerId, String postId, int page, int size) {
        requireAuthenticated(viewerId);
        postIdValidator.validate(postId);
        validatePagination(page, size);

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

        PageResult<PostLikeEntry> likersPage = postLikeRepository.findLikersByPostId(postId, page, size);
        List<ViewLikeUsersResult.LikeUserItem> items = likersPage.items().stream()
                .map(entry -> likeUserEnricher.enrich(entry.userId(), entry.likedAt()))
                .toList();

        return ViewLikeUsersResult.from(new PageResult<>(
                items,
                likersPage.page(),
                likersPage.size(),
                likersPage.totalElements(),
                likersPage.totalPages(),
                likersPage.hasNext()
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
}