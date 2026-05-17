package com.twohands.auth_service.application.rbac.assignrolestousers;

import com.twohands.auth_service.domain.rbac.AuthorizationDomainService;
import com.twohands.auth_service.domain.rbac.PermissionQueryRepository;
import com.twohands.auth_service.domain.rbac.RbacDomainError;
import com.twohands.auth_service.domain.rbac.Role;
import com.twohands.auth_service.domain.rbac.RoleAssignmentDomainService;
import com.twohands.auth_service.domain.rbac.RoleRepository;
import com.twohands.auth_service.domain.rbac.UserRoleAssignment;
import com.twohands.auth_service.domain.rbac.UserRoleAssignmentRepository;
import com.twohands.auth_service.domain.session.RefreshTokenSessionRepository;
import com.twohands.auth_service.domain.user.User;
import com.twohands.auth_service.domain.user.UserRepository;
import com.twohands.auth_service.domain.user.UserStatus;
import com.twohands.auth_service.exception.AppException;
import com.twohands.auth_service.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@Service
public class AssignRolesToUsersUseCase {

    private static final String REQUIRED_PERMISSION_CODE = "ASSIGN_ROLE";
    private static final String SUCCESS_MESSAGE = "Gan role cho user thanh cong.";

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleAssignmentRepository userRoleAssignmentRepository;
    private final PermissionQueryRepository permissionQueryRepository;
    private final RoleAssignmentDomainService roleAssignmentDomainService;
    private final AuthorizationDomainService authorizationDomainService;
    private final RefreshTokenSessionRepository refreshTokenSessionRepository;

    public AssignRolesToUsersUseCase(
            UserRepository userRepository,
            RoleRepository roleRepository,
            UserRoleAssignmentRepository userRoleAssignmentRepository,
            PermissionQueryRepository permissionQueryRepository,
            RefreshTokenSessionRepository refreshTokenSessionRepository
    ) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.userRoleAssignmentRepository = userRoleAssignmentRepository;
        this.permissionQueryRepository = permissionQueryRepository;
        this.roleAssignmentDomainService = new RoleAssignmentDomainService();
        this.authorizationDomainService = new AuthorizationDomainService();
        this.refreshTokenSessionRepository = refreshTokenSessionRepository;
    }

    @Transactional
    public AssignRolesToUsersResult execute(AssignRolesToUsersCommand command) {
        UUID actorUserId = requireActor(command.actorUserId());
        UUID targetUserId = command.targetUserId();
        UUID roleId = command.roleId();

        ensureActorHasAssignRolePermission(actorUserId);
        User targetUser = loadTargetUser(targetUserId);
        Role role = loadRole(roleId);

        ensureAllowedByDomainPolicy(actorUserId, targetUser.id());
        UserRoleAssignment assignment = userRoleAssignmentRepository.findByUserId(targetUser.id())
                .orElseGet(() -> new UserRoleAssignment(targetUser.id(), Set.of(), Instant.now(), Instant.now()));

        if (assignment.hasRole(role.id())) {
            throw new AppException(
                    ErrorCode.CONFLICT,
                    ErrorCode.CONFLICT.defaultMessage(),
                    "role_id",
                    "ALREADY_ASSIGNED"
            );
        }

        assignment.assignRole(role.id(), Instant.now());
        userRoleAssignmentRepository.save(assignment);

        // Revoke active sessions so target user must refresh claims.
        refreshTokenSessionRepository.revokeAllByUserId(targetUser.id());

        return new AssignRolesToUsersResult(targetUser.id(), role.id());
    }

    public String successMessage() {
        return SUCCESS_MESSAGE;
    }

    private UUID requireActor(UUID actorUserId) {
        if (actorUserId == null) {
            throw new AppException(ErrorCode.UNAUTHORIZED, ErrorCode.UNAUTHORIZED.defaultMessage());
        }
        return actorUserId;
    }

    private void ensureActorHasAssignRolePermission(UUID actorUserId) {
        Set<String> actorPermissions = permissionQueryRepository.findPermissionCodesByUserId(actorUserId);
        if (!authorizationDomainService.hasPermission(actorPermissions, REQUIRED_PERMISSION_CODE)) {
            throw new AppException(ErrorCode.FORBIDDEN, ErrorCode.FORBIDDEN.defaultMessage());
        }
    }

    private User loadTargetUser(UUID targetUserId) {
        User user = userRepository.findById(targetUserId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, ErrorCode.RESOURCE_NOT_FOUND.defaultMessage()));
        if (user.status() == UserStatus.DELETED) {
            throw new AppException(ErrorCode.RESOURCE_NOT_FOUND, ErrorCode.RESOURCE_NOT_FOUND.defaultMessage());
        }
        return user;
    }

    private Role loadRole(UUID roleId) {
        return roleRepository.findById(roleId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, ErrorCode.RESOURCE_NOT_FOUND.defaultMessage()));
    }

    private void ensureAllowedByDomainPolicy(UUID actorUserId, UUID targetUserId) {
        try {
            roleAssignmentDomainService.ensureCanAssignRole(actorUserId, targetUserId);
        } catch (RbacDomainError ex) {
            if ("RBAC_SELF_ASSIGN_FORBIDDEN".equals(ex.code())) {
                throw new AppException(ErrorCode.FORBIDDEN, ErrorCode.FORBIDDEN.defaultMessage());
            }
            throw new AppException(ErrorCode.BAD_REQUEST, ex.getMessage());
        }
    }
}
