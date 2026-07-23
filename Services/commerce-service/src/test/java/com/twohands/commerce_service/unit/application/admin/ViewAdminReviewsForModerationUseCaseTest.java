package com.twohands.commerce_service.unit.application.admin;

import com.twohands.commerce_service.application.admin.viewadminreviews.ViewAdminReviewsForModerationCommand;
import com.twohands.commerce_service.application.admin.viewadminreviews.ViewAdminReviewsForModerationUseCase;
import com.twohands.commerce_service.common.pagination.PageQuery;
import com.twohands.commerce_service.domain.admin.AdminReviewListEntry;
import com.twohands.commerce_service.domain.admin.AdminReviewListSort;
import com.twohands.commerce_service.domain.admin.ViewAdminReviewsForModerationRepository;
import com.twohands.commerce_service.domain.admin.ViewAdminReviewsForModerationResult;
import com.twohands.commerce_service.domain.review.ReviewStatus;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ViewAdminReviewsForModerationUseCaseTest {

    @Mock
    private ViewAdminReviewsForModerationRepository repository;

    private ViewAdminReviewsForModerationUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new ViewAdminReviewsForModerationUseCase(repository);
    }

    @Test
    void shouldDefaultSortToNewest() {
        AdminReviewListEntry entry = sampleEntry();

        when(repository.count(Optional.empty(), Optional.empty(), Optional.empty())).thenReturn(1L);
        when(repository.find(
                eq(Optional.empty()),
                eq(Optional.empty()),
                eq(Optional.empty()),
                eq(AdminReviewListSort.NEWEST),
                any(PageQuery.class)
        )).thenReturn(List.of(entry));

        ViewAdminReviewsForModerationResult result = useCase.execute(
                new ViewAdminReviewsForModerationCommand(1, 20, null, null, null, null)
        );

        assertThat(result.items()).containsExactly(entry);
        verify(repository).find(
                eq(Optional.empty()),
                eq(Optional.empty()),
                eq(Optional.empty()),
                eq(AdminReviewListSort.NEWEST),
                any(PageQuery.class)
        );
    }

    @Test
    void shouldParseRatingDescSort() {
        when(repository.count(Optional.empty(), Optional.empty(), Optional.empty())).thenReturn(1L);
        when(repository.find(
                eq(Optional.empty()),
                eq(Optional.empty()),
                eq(Optional.empty()),
                eq(AdminReviewListSort.RATING_DESC),
                any(PageQuery.class)
        )).thenReturn(List.of());

        useCase.execute(new ViewAdminReviewsForModerationCommand(1, 20, null, null, null, "RATING_DESC"));

        verify(repository).find(
                eq(Optional.empty()),
                eq(Optional.empty()),
                eq(Optional.empty()),
                eq(AdminReviewListSort.RATING_DESC),
                any(PageQuery.class)
        );
    }

    @Test
    void shouldRejectInvalidSort() {
        assertThatThrownBy(() -> useCase.execute(
                new ViewAdminReviewsForModerationCommand(1, 20, null, null, null, "INVALID")
        ))
                .isInstanceOf(AppException.class)
                .extracting(ex -> ((AppException) ex).getErrorCode())
                .isEqualTo(ErrorCode.VALIDATION_ERROR);
    }

    @Test
    void shouldAllowLimitUpToOneHundred() {
        when(repository.count(Optional.empty(), Optional.empty(), Optional.empty())).thenReturn(1L);
        when(repository.find(
                eq(Optional.empty()),
                eq(Optional.empty()),
                eq(Optional.empty()),
                eq(AdminReviewListSort.NEWEST),
                eq(new PageQuery(1, 100))
        )).thenReturn(List.of());

        useCase.execute(new ViewAdminReviewsForModerationCommand(1, 100, null, null, null, null));

        verify(repository).find(
                eq(Optional.empty()),
                eq(Optional.empty()),
                eq(Optional.empty()),
                eq(AdminReviewListSort.NEWEST),
                eq(new PageQuery(1, 100))
        );
    }

    @Test
    void shouldRejectLimitAboveOneHundred() {
        assertThatThrownBy(() -> useCase.execute(
                new ViewAdminReviewsForModerationCommand(1, 101, null, null, null, null)
        ))
                .isInstanceOf(AppException.class)
                .extracting(ex -> ((AppException) ex).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_PAGINATION);
    }

    private AdminReviewListEntry sampleEntry() {
        return new AdminReviewListEntry(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                "Vintage Jacket",
                "https://example.com/thumb.jpg",
                UUID.randomUUID(),
                UUID.randomUUID(),
                5,
                "Great item",
                ReviewStatus.VISIBLE,
                Instant.parse("2026-05-01T08:00:00Z")
        );
    }
}
