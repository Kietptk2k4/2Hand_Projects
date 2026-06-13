package com.twohands.auth_service.unit.application.admin.viewuserinvestigationprofile;

import com.twohands.auth_service.application.admin.viewuserinvestigationprofile.ViewUserInvestigationProfileByAdminCommand;
import com.twohands.auth_service.application.admin.viewuserinvestigationprofile.ViewUserInvestigationProfileByAdminUseCase;
import com.twohands.auth_service.domain.rbac.PermissionQueryRepository;
import com.twohands.auth_service.domain.user.EmailAddress;
import com.twohands.auth_service.domain.user.PasswordHash;
import com.twohands.auth_service.domain.user.User;
import com.twohands.auth_service.domain.user.UserProfile;
import com.twohands.auth_service.domain.user.UserProfileRepository;
import com.twohands.auth_service.domain.user.UserRepository;
import com.twohands.auth_service.domain.user.UserStatus;
import com.twohands.auth_service.exception.AppException;
import com.twohands.auth_service.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ViewUserInvestigationProfileByAdminUseCaseTest {

	private final UserRepository userRepository = mock(UserRepository.class);
	private final UserProfileRepository userProfileRepository = mock(UserProfileRepository.class);
	private final PermissionQueryRepository permissionQueryRepository = mock(PermissionQueryRepository.class);

	private ViewUserInvestigationProfileByAdminUseCase useCase;

	@BeforeEach
	void setUp() {
		useCase = new ViewUserInvestigationProfileByAdminUseCase(
				userRepository,
				userProfileRepository,
				permissionQueryRepository
		);
	}

	@Test
	void shouldReturnInvestigationProfileForAuthorizedActor() {
		UUID actorId = UUID.randomUUID();
		UUID userId = UUID.randomUUID();
		Instant now = Instant.now();

		User user = User.rehydrate(
				userId,
				EmailAddress.of("investigate@example.com"),
				PasswordHash.of("hash"),
				UserStatus.ACTIVE,
				true,
				false,
				now.minusSeconds(60),
				null,
				null,
				now.minusSeconds(3600),
				now
		);
		UserProfile profile = UserProfile.rehydrate(
				userId,
				"Investigation User",
				"https://cdn.example/avatar.png",
				null,
				"Bio",
				"https://example.com",
				java.util.Map.of(),
				false,
				now,
				now
		);

		when(permissionQueryRepository.findPermissionCodesByUserId(actorId)).thenReturn(Set.of("USER_INVESTIGATION_READ"));
		when(userRepository.findById(userId)).thenReturn(Optional.of(user));
		when(userProfileRepository.findByUserId(userId)).thenReturn(Optional.of(profile));

		var result = useCase.execute(new ViewUserInvestigationProfileByAdminCommand(actorId, userId));

		assertThat(result.email()).isEqualTo("investigate@example.com");
		assertThat(result.displayName()).isEqualTo("Investigation User");
		assertThat(result.status()).isEqualTo("ACTIVE");
	}

	@Test
	void shouldRejectMissingPermission() {
		UUID actorId = UUID.randomUUID();
		when(permissionQueryRepository.findPermissionCodesByUserId(actorId)).thenReturn(Set.of("USER_READ"));

		assertThatThrownBy(() -> useCase.execute(new ViewUserInvestigationProfileByAdminCommand(actorId, UUID.randomUUID())))
				.isInstanceOf(AppException.class)
				.extracting(ex -> ((AppException) ex).getErrorCode())
				.isEqualTo(ErrorCode.FORBIDDEN);
	}
}
