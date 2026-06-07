package com.twohands.social_service.application.user.viewsuggestedusers;

import com.twohands.social_service.domain.follow.FollowRepository;
import com.twohands.social_service.domain.post.PageResult;
import com.twohands.social_service.domain.suggesteduser.SuggestedUsersRepository;
import com.twohands.social_service.domain.user.UserProjection;
import com.twohands.social_service.domain.user.UserProjectionRepository;
import com.twohands.social_service.exception.AppException;
import com.twohands.social_service.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class ViewSuggestedUsersUseCase {

    private static final int MIN_PAGE = 0;
    private static final int MIN_SIZE = 1;
    private static final int MAX_SIZE = 50;
    private static final int MAX_CANDIDATE_SCAN = 200;

    private final FollowRepository followRepository;
    private final UserProjectionRepository userProjectionRepository;
    private final SuggestedUsersRepository suggestedUsersRepository;

    public ViewSuggestedUsersUseCase(
            FollowRepository followRepository,
            UserProjectionRepository userProjectionRepository,
            SuggestedUsersRepository suggestedUsersRepository
    ) {
        this.followRepository = followRepository;
        this.userProjectionRepository = userProjectionRepository;
        this.suggestedUsersRepository = suggestedUsersRepository;
    }

    @Transactional(readOnly = true)
    public ViewSuggestedUsersResult execute(ViewSuggestedUsersCommand command) {
        if (command.viewerId() == null) {
            throw new AppException(ErrorCode.UNAUTHORIZED, "Authentication required");
        }
        validatePagination(command.page(), command.size());

        List<String> excludeUserIds = buildExcludeUserIds(command.viewerId());
        List<UserProjection> candidates = userProjectionRepository.findActiveSuggestionCandidatesExcluding(
                excludeUserIds,
                MAX_CANDIDATE_SCAN
        );

        List<UUID> candidateIds = candidates.stream()
                .map(projection -> UUID.fromString(projection.userId()))
                .toList();

        Map<UUID, Long> mutualCounts = suggestedUsersRepository.findMutualFollowCounts(
                command.viewerId(),
                candidateIds
        );

        List<ScoredCandidate> scoredCandidates = candidates.stream()
                .map(projection -> new ScoredCandidate(
                        projection,
                        mutualCounts.getOrDefault(UUID.fromString(projection.userId()), 0L)
                ))
                .sorted(Comparator
                        .comparingLong(ScoredCandidate::mutualFollowCount).reversed()
                        .thenComparing(
                                candidate -> candidate.projection().displayName(),
                                Comparator.nullsLast(String::compareToIgnoreCase)
                        ))
                .toList();

        int start = command.page() * command.size();
        if (start >= scoredCandidates.size()) {
            return emptyResult(command.page(), command.size(), scoredCandidates.size());
        }

        int end = Math.min(start + command.size(), scoredCandidates.size());
        List<ViewSuggestedUsersResult.SuggestedUserItem> items = new ArrayList<>();
        for (ScoredCandidate candidate : scoredCandidates.subList(start, end)) {
            UserProjection projection = candidate.projection();
            items.add(new ViewSuggestedUsersResult.SuggestedUserItem(
                    projection.userId(),
                    projection.displayName(),
                    projection.avatarUrl(),
                    "NONE",
                    candidate.mutualFollowCount()
            ));
        }

        long totalElements = scoredCandidates.size();
        long totalPages = totalElements == 0 ? 0 : (totalElements + command.size() - 1) / command.size();
        boolean hasNext = end < scoredCandidates.size();

        PageResult<ViewSuggestedUsersResult.SuggestedUserItem> userPage = new PageResult<>(
                items,
                command.page(),
                command.size(),
                totalElements,
                totalPages,
                hasNext
        );

        return new ViewSuggestedUsersResult(userPage);
    }

    public String successMessage() {
        return "Lay goi y nguoi dung thanh cong.";
    }

    private List<String> buildExcludeUserIds(UUID viewerId) {
        List<String> excluded = new ArrayList<>();
        excluded.add(viewerId.toString());
        followRepository.findFolloweeIdsByFollowerId(viewerId).stream()
                .map(UUID::toString)
                .forEach(excluded::add);
        return excluded;
    }

    private ViewSuggestedUsersResult emptyResult(int page, int size, long totalElements) {
        long totalPages = totalElements == 0 ? 0 : (totalElements + size - 1) / size;
        return new ViewSuggestedUsersResult(new PageResult<>(
                List.of(),
                page,
                size,
                totalElements,
                totalPages,
                false
        ));
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

    private record ScoredCandidate(UserProjection projection, long mutualFollowCount) {
    }
}