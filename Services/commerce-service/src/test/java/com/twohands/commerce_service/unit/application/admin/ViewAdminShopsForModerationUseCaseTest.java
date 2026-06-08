package com.twohands.commerce_service.unit.application.admin;

import com.twohands.commerce_service.application.admin.viewadminshops.ViewAdminShopsForModerationCommand;
import com.twohands.commerce_service.application.admin.viewadminshops.ViewAdminShopsForModerationUseCase;
import com.twohands.commerce_service.common.pagination.PageQuery;
import com.twohands.commerce_service.domain.admin.AdminShopListEntry;
import com.twohands.commerce_service.domain.admin.AdminShopListSort;
import com.twohands.commerce_service.domain.admin.ViewAdminShopsForModerationRepository;
import com.twohands.commerce_service.domain.admin.ViewAdminShopsForModerationResult;
import com.twohands.commerce_service.domain.shop.ShopStatus;
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
class ViewAdminShopsForModerationUseCaseTest {

    @Mock
    private ViewAdminShopsForModerationRepository repository;

    private ViewAdminShopsForModerationUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new ViewAdminShopsForModerationUseCase(repository);
    }

    @Test
    void shouldReturnPaginatedShops() {
        AdminShopListEntry entry = new AdminShopListEntry(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "Demo Shop",
                "https://example.com/logo.png",
                ShopStatus.ACTIVE,
                Instant.parse("2026-05-01T08:00:00Z")
        );

        when(repository.count(Optional.empty(), Optional.empty())).thenReturn(1L);
        when(repository.find(
                eq(Optional.empty()),
                eq(Optional.empty()),
                eq(AdminShopListSort.NEWEST),
                any(PageQuery.class)
        )).thenReturn(List.of(entry));

        ViewAdminShopsForModerationResult result = useCase.execute(
                new ViewAdminShopsForModerationCommand(1, 20, "all", null, "NEWEST")
        );

        assertThat(result.items()).containsExactly(entry);
        assertThat(result.pagination().totalItems()).isEqualTo(1);
        assertThat(result.pagination().hasNext()).isFalse();
    }

    @Test
    void shouldRejectInvalidPagination() {
        assertThatThrownBy(() -> useCase.execute(
                new ViewAdminShopsForModerationCommand(0, 20, null, null, null)
        ))
                .isInstanceOf(AppException.class)
                .extracting(ex -> ((AppException) ex).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_PAGINATION);
    }
}
