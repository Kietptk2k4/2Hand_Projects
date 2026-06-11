package com.twohands.auth_service.unit.application.admin.viewuserlistforinvestigation;

import com.twohands.auth_service.application.admin.viewuserlistforinvestigation.ViewUserListForInvestigationCommand;
import com.twohands.auth_service.application.admin.viewuserlistforinvestigation.ViewUserListForInvestigationUseCase;
import com.twohands.auth_service.application.rbac.viewuserlistforrbac.ViewUserListForRbacResult;
import com.twohands.auth_service.domain.rbac.PermissionQueryRepository;
import com.twohands.auth_service.domain.rbac.RbacUserListCriteria;
import com.twohands.auth_service.domain.rbac.RbacUserListItem;
import com.twohands.auth_service.domain.rbac.RbacUserListPagedResult;
import com.twohands.auth_service.domain.rbac.RbacUserListRepository;
import com.twohands.auth_service.domain.rbac.RbacUserListSortField;
import com.twohands.auth_service.exception.AppException;
import com.twohands.auth_service.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ViewUserListForInvestigationUseCaseTest {

    private final RbacUserListRepository rbacUserListRepository = Mockito.mock(RbacUserListRepository.class);
    private final PermissionQueryRepository permissionQueryRepository = Mockito.mock(PermissionQueryRepository.class);

    private ViewUserListForInvestigationUseCase useCase;
    private UUID actorUserId;

    @BeforeEach
    void setup() {
        useCase = new ViewUserListForInvestigationUseCase(rbacUserListRepository, permissionQueryRepository);
        actorUserId = UUID.randomUUID();
    }

    @Test
    void shouldReturnUserListWithCreatedAtDefaultSort() {
        UUID userId = UUID.randomUUID();
        when(permissionQueryRepository.findPermissionCodesByUserId(actorUserId))
                .thenReturn(Set.of("USER_INVESTIGATION_READ"));
        when(rbacUserListRepository.findPage(any())).thenReturn(
                new RbacUserListPagedResult(
                        List.of(new RbacUserListItem(
                                userId,
                                "active@2hands.vn",
                                "Active User",
                                "ACTIVE",
                                List.of("USER"),
                                Instant.parse("2026-05-17T10:00:00Z")
                        )),
                        1,
                        20,
                        1L,
                        1,
                        false
                )
        );

        ViewUserListForRbacResult result = useCase.execute(
                new ViewUserListForInvestigationCommand(actorUserId, null, null, null, 1, 20)
        );

        assertEquals(1, result.items().size());
        assertEquals("active@2hands.vn", result.items().get(0).email());

        ArgumentCaptor<RbacUserListCriteria> captor = ArgumentCaptor.forClass(RbacUserListCriteria.class);
        verify(rbacUserListRepository).findPage(captor.capture());
        assertEquals(RbacUserListSortField.CREATED_AT, captor.getValue().sortField());
    }

    @Test
    void shouldReturnForbiddenWhenActorHasNoPermission() {
        when(permissionQueryRepository.findPermissionCodesByUserId(actorUserId)).thenReturn(Set.of("ASSIGN_ROLE"));

        AppException ex = assertThrows(
                AppException.class,
                () -> useCase.execute(new ViewUserListForInvestigationCommand(actorUserId, null, null, null, 1, 20))
        );

        assertEquals(ErrorCode.FORBIDDEN, ex.getErrorCode());
    }

    @Test
    void shouldApplyStatusAndQueryFilters() {
        when(permissionQueryRepository.findPermissionCodesByUserId(actorUserId))
                .thenReturn(Set.of("USER_INVESTIGATION_READ"));
        when(rbacUserListRepository.findPage(any())).thenReturn(
                new RbacUserListPagedResult(List.of(), 1, 20, 0L, 0, false)
        );

        useCase.execute(new ViewUserListForInvestigationCommand(actorUserId, "SUSPENDED", "spam@", "email", 2, 10));

        ArgumentCaptor<RbacUserListCriteria> captor = ArgumentCaptor.forClass(RbacUserListCriteria.class);
        verify(rbacUserListRepository).findPage(captor.capture());
        assertEquals(Optional.of("SUSPENDED"), captor.getValue().status());
        assertEquals(Optional.of("spam@"), captor.getValue().emailFragment());
        assertEquals(RbacUserListSortField.EMAIL, captor.getValue().sortField());
        assertEquals(2, captor.getValue().page());
        assertEquals(10, captor.getValue().size());
    }
}
