package com.twohands.social_service.unit.application.admin.viewpostlistformoderation;

import com.twohands.social_service.application.admin.viewpostlistformoderation.ViewPostListForModerationCommand;
import com.twohands.social_service.application.admin.viewpostlistformoderation.ViewPostListForModerationResult;
import com.twohands.social_service.application.admin.viewpostlistformoderation.ViewPostListForModerationUseCase;
import com.twohands.social_service.domain.admin.AdminModerationListSortField;
import com.twohands.social_service.domain.admin.AdminPostListCriteria;
import com.twohands.social_service.domain.admin.AdminPostListItem;
import com.twohands.social_service.domain.admin.AdminPostListRepository;
import com.twohands.social_service.domain.post.PageResult;
import com.twohands.social_service.exception.AppException;
import com.twohands.social_service.exception.ErrorCode;
import com.twohands.social_service.security.AuthenticatedUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ViewPostListForModerationUseCaseTest {

    private final AdminPostListRepository adminPostListRepository = Mockito.mock(AdminPostListRepository.class);
    private ViewPostListForModerationUseCase useCase;
    private AuthenticatedUser actor;

    @BeforeEach
    void setup() {
        useCase = new ViewPostListForModerationUseCase(adminPostListRepository);
        actor = new AuthenticatedUser(UUID.randomUUID(), List.of("ADMIN"), List.of("POST_MODERATE"));
    }

    @Test
    void shouldReturnPostListWithCreatedAtDefaultSort() {
        when(adminPostListRepository.findPage(any())).thenReturn(
                new PageResult<>(
                        List.of(new AdminPostListItem(
                                "507f1f77bcf86cd799439011",
                                UUID.randomUUID().toString(),
                                "Caption",
                                "ACTIVE",
                                "NONE",
                                3L,
                                Instant.parse("2026-05-17T10:00:00Z"),
                                Instant.parse("2026-05-17T11:00:00Z")
                        )),
                        1,
                        20,
                        1L,
                        1,
                        false
                )
        );

        ViewPostListForModerationResult result = useCase.execute(
                new ViewPostListForModerationCommand(actor, null, null, null, null, 1, 20)
        );

        assertEquals(1, result.items().size());
        assertEquals("507f1f77bcf86cd799439011", result.items().get(0).id());

        ArgumentCaptor<AdminPostListCriteria> captor = ArgumentCaptor.forClass(AdminPostListCriteria.class);
        verify(adminPostListRepository).findPage(captor.capture());
        assertEquals(AdminModerationListSortField.CREATED_AT, captor.getValue().sortField());
    }

    @Test
    void shouldReturnForbiddenWhenActorHasNoAccess() {
        AuthenticatedUser regularUser = new AuthenticatedUser(UUID.randomUUID(), List.of("USER"), List.of());

        AppException ex = assertThrows(
                AppException.class,
                () -> useCase.execute(new ViewPostListForModerationCommand(regularUser, null, null, null, null, 1, 20))
        );

        assertEquals(ErrorCode.FORBIDDEN, ex.getErrorCode());
    }

    @Test
    void shouldApplyFilters() {
        when(adminPostListRepository.findPage(any())).thenReturn(
                new PageResult<>(List.of(), 1, 20, 0L, 0, false)
        );

        useCase.execute(new ViewPostListForModerationCommand(
                actor, "ACTIVE", "HIDDEN", "spam", "updated_at", 2, 10
        ));

        ArgumentCaptor<AdminPostListCriteria> captor = ArgumentCaptor.forClass(AdminPostListCriteria.class);
        verify(adminPostListRepository).findPage(captor.capture());
        assertEquals(Optional.of("ACTIVE"), captor.getValue().status());
        assertEquals(Optional.of("HIDDEN"), captor.getValue().moderationStatus());
        assertEquals(Optional.of("spam"), captor.getValue().query());
        assertEquals(AdminModerationListSortField.UPDATED_AT, captor.getValue().sortField());
    }
}
