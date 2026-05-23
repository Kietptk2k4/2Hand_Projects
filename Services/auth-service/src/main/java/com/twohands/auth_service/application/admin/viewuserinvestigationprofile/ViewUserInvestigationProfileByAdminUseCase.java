package com.twohands.auth_service.application.admin.viewuserinvestigationprofile;

import com.twohands.auth_service.domain.rbac.AuthorizationDomainService;
import com.twohands.auth_service.domain.rbac.PermissionQueryRepository;
import com.twohands.auth_service.domain.user.User;
import com.twohands.auth_service.domain.user.UserProfile;
import com.twohands.auth_service.domain.user.UserProfileRepository;
import com.twohands.auth_service.domain.user.UserRepository;
import com.twohands.auth_service.domain.user.UserStatus;
import com.twohands.auth_service.exception.AppException;
import com.twohands.auth_service.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.UUID;

@Service
public class ViewUserInvestigationProfileByAdminUseCase {

	private static final String USER_INVESTIGATION_READ_PERMISSION = "USER_INVESTIGATION_READ";
	private static final String SUCCESS_MESSAGE = "User investigation profile retrieved successfully";

	private final UserRepository userRepository;
	private final UserProfileRepository userProfileRepository;
	private final PermissionQueryRepository permissionQueryRepository;
	private final AuthorizationDomainService authorizationDomainService;

	public ViewUserInvestigationProfileByAdminUseCase(
			UserRepository userRepository,
			UserProfileRepository userProfileRepository,
			PermissionQueryRepository permissionQueryRepository
	) {
		this.userRepository = userRepository;
		this.userProfileRepository = userProfileRepository;
		this.permissionQueryRepository = permissionQueryRepository;
		this.authorizationDomainService = new AuthorizationDomainService();
	}

	@Transactional(readOnly = true)
	public ViewUserInvestigationProfileByAdminResult execute(ViewUserInvestigationProfileByAdminCommand command) {
		UUID actorAdminId = requireActor(command.actorAdminId());
		ensureActorCanViewInvestigation(actorAdminId);

		User user = userRepository.findById(command.targetUserId())
				.orElseThrow(() -> new AppException(
						ErrorCode.RESOURCE_NOT_FOUND,
						ErrorCode.RESOURCE_NOT_FOUND.defaultMessage()
				));
		if (user.status() == UserStatus.DELETED) {
			throw new AppException(ErrorCode.RESOURCE_NOT_FOUND, ErrorCode.RESOURCE_NOT_FOUND.defaultMessage());
		}

		UserProfile profile = userProfileRepository.findByUserId(command.targetUserId())
				.orElseThrow(() -> new AppException(
						ErrorCode.RESOURCE_NOT_FOUND,
						ErrorCode.RESOURCE_NOT_FOUND.defaultMessage()
				));

		return new ViewUserInvestigationProfileByAdminResult(
				user.id(),
				user.email().value(),
				user.status().name(),
				user.emailVerified(),
				user.phoneVerified(),
				user.lastLoginAt(),
				user.createdAt(),
				profile.displayName(),
				profile.avatarUrl(),
				profile.bio(),
				profile.website(),
				profile.isPrivate()
		);
	}

	public String successMessage() {
		return SUCCESS_MESSAGE;
	}

	private UUID requireActor(UUID actorAdminId) {
		if (actorAdminId == null) {
			throw new AppException(ErrorCode.UNAUTHORIZED, ErrorCode.UNAUTHORIZED.defaultMessage());
		}
		return actorAdminId;
	}

	private void ensureActorCanViewInvestigation(UUID actorAdminId) {
		Set<String> permissions = permissionQueryRepository.findPermissionCodesByUserId(actorAdminId);
		if (!authorizationDomainService.hasPermission(permissions, USER_INVESTIGATION_READ_PERMISSION)) {
			throw new AppException(ErrorCode.FORBIDDEN, ErrorCode.FORBIDDEN.defaultMessage());
		}
	}
}
