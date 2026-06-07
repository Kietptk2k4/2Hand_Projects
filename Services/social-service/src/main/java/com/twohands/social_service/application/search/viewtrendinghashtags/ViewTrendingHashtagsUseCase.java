package com.twohands.social_service.application.search.viewtrendinghashtags;

import com.twohands.social_service.domain.trendinghashtag.TrendingHashtag;
import com.twohands.social_service.domain.trendinghashtag.TrendingHashtagsRepository;
import com.twohands.social_service.exception.AppException;
import com.twohands.social_service.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class ViewTrendingHashtagsUseCase {

    public static final int DEFAULT_LIMIT = 5;
    public static final int MIN_LIMIT = 1;
    public static final int MAX_LIMIT = 20;
    public static final int WINDOW_DAYS = 7;
    public static final int POST_COUNT_WEIGHT = 10;

    private final TrendingHashtagsRepository trendingHashtagsRepository;

    public ViewTrendingHashtagsUseCase(TrendingHashtagsRepository trendingHashtagsRepository) {
        this.trendingHashtagsRepository = trendingHashtagsRepository;
    }

    @Transactional(readOnly = true)
    public ViewTrendingHashtagsResult execute(ViewTrendingHashtagsCommand command) {
        if (command.userId() == null) {
            throw new AppException(ErrorCode.UNAUTHORIZED, "Authentication required");
        }

        int limit = resolveLimit(command.limit());
        Instant createdAfter = Instant.now().minus(WINDOW_DAYS, ChronoUnit.DAYS);

        List<ViewTrendingHashtagsResult.TrendingHashtagItem> items = trendingHashtagsRepository
                .findTrendingHashtags(createdAfter, limit, POST_COUNT_WEIGHT)
                .stream()
                .map(this::toItem)
                .toList();

        return new ViewTrendingHashtagsResult(items);
    }

    public String successMessage() {
        return "Lay hashtag thi hanh thanh cong.";
    }

    private int resolveLimit(int limit) {
        if (limit <= 0) {
            return DEFAULT_LIMIT;
        }
        if (limit < MIN_LIMIT || limit > MAX_LIMIT) {
            throw new AppException(
                    ErrorCode.INVALID_PAGINATION,
                    "Tham so limit khong hop le.",
                    "limit",
                    "MUST_BE_BETWEEN_1_AND_20"
            );
        }
        return limit;
    }

    private ViewTrendingHashtagsResult.TrendingHashtagItem toItem(TrendingHashtag hashtag) {
        return new ViewTrendingHashtagsResult.TrendingHashtagItem(
                hashtag.tag(),
                hashtag.postCount(),
                hashtag.totalLikes(),
                hashtag.totalReplies(),
                hashtag.engagementCount(),
                hashtag.score()
        );
    }
}