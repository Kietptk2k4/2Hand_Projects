package com.twohands.auth_service.unit.application.admin.searchusersforinvestigation;

import com.twohands.auth_service.application.admin.searchusersforinvestigation.SearchUsersForInvestigationCommand;
import com.twohands.auth_service.application.admin.searchusersforinvestigation.SearchUsersForInvestigationUseCase;
import com.twohands.auth_service.domain.rbac.PermissionQueryRepository;
import com.twohands.auth_service.domain.user.InvestigationUserSearchItem;
import com.twohands.auth_service.domain.user.UserInvestigationSearchRepository;
import com.twohands.auth_service.exception.AppException;
import com.twohands.auth_service.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SearchUsersForInvestigationUseCaseTest {

	private final UserInvestigationSearchRepository userInvestigationSearchRepository =
			mock(UserInvestigationSearchRepository.class);
	private final PermissionQueryRepository permissionQueryRepository = mock(PermissionQueryRepository.class);

	private SearchUsersForInvestigationUseCase useCase;

	@BeforeEach
	void setUp() {
		useCase = new SearchUsersForInvestigationUseCase(
				userInvestigationSearchRepository,
				permissionQueryRepository
		);
	}

	@Test
	void shouldSearchByEmailFragmentForAuthorizedActor() {
		UUID actorId = UUID.randomUUID();
		InvestigationUserSearchItem item = new InvestigationUserSearchItem(
				UUID.randomUUID(),
				"active@2hands.vn",
				"Active User",
				"ACTIVE",
				List.of("USER")
		);

		when(permissionQueryRepository.findPermissionCodesByUserId(actorId))
				.thenReturn(Set.of("USER_INVESTIGATION_READ"));
		when(userInvestigationSearchRepository.searchByEmailFragment("active@2hands.vn", 20))
				.thenReturn(List.of(item));

		var result = useCase.execute(new SearchUsersForInvestigationCommand(actorId, "active@2hands.vn", 20));

		assertThat(result.users()).containsExactly(item);
		verify(userInvestigationSearchRepository).searchByEmailFragment("active@2hands.vn", 20);
	}

	@Test
	void shouldSearchByUserIdWhenQueryIsUuid() {
		UUID actorId = UUID.randomUUID();
		UUID targetId = UUID.randomUUID();
		InvestigationUserSearchItem item = new InvestigationUserSearchItem(
				targetId,
				"pending@2hands.vn",
				"Pending User",
				"PENDING_VERIFICATION",
				List.of("USER")
		);

		when(permissionQueryRepository.findPermissionCodesByUserId(actorId))
				.thenReturn(Set.of("USER_INVESTIGATION_READ"));
		when(userInvestigationSearchRepository.findByUserId(targetId)).thenReturn(List.of(item));

		var result = useCase.execute(new SearchUsersForInvestigationCommand(actorId, targetId.toString(), 20));

		assertThat(result.users()).containsExactly(item);
		verify(userInvestigationSearchRepository).findByUserId(targetId);
	}

	@Test
	void shouldRejectUnauthorizedActor() {
		assertThatThrownBy(() -> useCase.execute(new SearchUsersForInvestigationCommand(null, "active", 20)))
				.isInstanceOf(AppException.class)
				.extracting(ex -> ((AppException) ex).getErrorCode())
				.isEqualTo(ErrorCode.UNAUTHORIZED);
	}

	@Test
	void shouldRejectMissingPermission() {
		UUID actorId = UUID.randomUUID();
		when(permissionQueryRepository.findPermissionCodesByUserId(actorId)).thenReturn(Set.of());

		assertThatThrownBy(() -> useCase.execute(new SearchUsersForInvestigationCommand(actorId, "active", 20)))
				.isInstanceOf(AppException.class)
				.extracting(ex -> ((AppException) ex).getErrorCode())
				.isEqualTo(ErrorCode.FORBIDDEN);
	}
}
