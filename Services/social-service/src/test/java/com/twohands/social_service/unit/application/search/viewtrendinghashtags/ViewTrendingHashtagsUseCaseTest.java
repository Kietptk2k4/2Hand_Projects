package com.twohands.social_service.unit.application.search.viewtrendinghashtags;

import com.twohands.social_service.application.search.viewtrendinghashtags.ViewTrendingHashtagsCommand;
import com.twohands.social_service.application.search.viewtrendinghashtags.ViewTrendingHashtagsResult;
import com.twohands.social_service.application.search.viewtrendinghashtags.ViewTrendingHashtagsUseCase;
import com.twohands.social_service.domain.trendinghashtag.TrendingHashtag;
import com.twohands.social_service.domain.trendinghashtag.TrendingHashtagsRepository;
import com.twohands.social_service.exception.AppException;
import com.twohands.social_service.exception.ErrorCode;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ViewTrendingHashtagsUseCaseTest {

    private final TrendingHashtagsRepository trendingHashtagsRepository = mock(TrendingHashtagsRepository.class);
    private final ViewTrendingHashtagsUseCase useCase = new ViewTrendingHashtagsUseCase(trendingHashtagsRepository);

    @Test
    void shouldReturnTrendingHashtagsForViewer() {
        UUID viewerId = UUID.randomUUID();

        when(trendingHashtagsRepository.findTrendingHashtags(
                org.mockito.ArgumentMatchers.any(Instant.class),
                eq(5),
                eq(ViewTrendingHashtagsUseCase.POST_COUNT_WEIGHT)
        )).thenReturn(List.of(
                new TrendingHashtag("LegalTech", 3L, 20L, 5L, 25L, 55L)
        ));

        ViewTrendingHashtagsResult result = useCase.execute(new ViewTrendingHashtagsCommand(viewerId, 5));

        assertThat(result.items()).hasSize(1);
        assertThat(result.items().getFirst().tag()).isEqualTo("LegalTech");
        assertThat(result.items().getFirst().postCount()).isEqualTo(3L);
        assertThat(result.items().getFirst().score()).isEqualTo(55L);

        ArgumentCaptor<Instant> createdAfterCaptor = ArgumentCaptor.forClass(Instant.class);
        verify(trendingHashtagsRepository).findTrendingHashtags(
                createdAfterCaptor.capture(),
                eq(5),
                eq(10)
        );
        assertThat(createdAfterCaptor.getValue()).isBefore(Instant.now());
    }

    @Test
    void shouldRequireAuthentication() {
        assertThatThrownBy(() -> useCase.execute(new ViewTrendingHashtagsCommand(null, 5)))
                .isInstanceOf(AppException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.UNAUTHORIZED);
    }

    @Test
    void shouldRejectInvalidLimit() {
        UUID viewerId = UUID.randomUUID();

        assertThatThrownBy(() -> useCase.execute(new ViewTrendingHashtagsCommand(viewerId, 25)))
                .isInstanceOf(AppException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_PAGINATION);
    }
}