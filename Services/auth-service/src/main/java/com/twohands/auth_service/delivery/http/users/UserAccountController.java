package com.twohands.auth_service.delivery.http.users;

import com.twohands.auth_service.application.useraccount.softdelete.SoftDeleteAccountCommand;
import com.twohands.auth_service.application.useraccount.softdelete.SoftDeleteAccountUseCase;
import com.twohands.auth_service.application.useraccount.toggleprivacy.TogglePrivacyCommand;
import com.twohands.auth_service.application.useraccount.toggleprivacy.TogglePrivacyUseCase;
import com.twohands.auth_service.application.useraccount.logoutallsesssion.LogoutAllSesssionUseCase;
import com.twohands.auth_service.application.useraccount.trackloginhistory.TrackLoginHistoryResult;
import com.twohands.auth_service.application.useraccount.trackloginhistory.TrackLoginHistoryUseCase;
import com.twohands.auth_service.application.useraccount.updateavatar.UpdateAvatarCommand;
import com.twohands.auth_service.application.useraccount.updateavatar.UpdateAvatarUseCase;
import com.twohands.auth_service.application.useraccount.updateprofile.UpdateProfileCommand;
import com.twohands.auth_service.application.useraccount.updateprofile.UpdateProfileUseCase;
import com.twohands.auth_service.application.useraccount.updatesettings.UpdateUserSettingsCommand;
import com.twohands.auth_service.application.useraccount.updatesettings.UpdateUserSettingsResult;
import com.twohands.auth_service.application.useraccount.updatesettings.UpdateUserSettingsUseCase;
import com.twohands.auth_service.application.useraccount.viewaccount.ViewAccountResult;
import com.twohands.auth_service.application.useraccount.viewaccount.ViewAccountUseCase;
import com.twohands.auth_service.application.useraccount.viewloginsesssionlist.ViewLoginSesssionListResult;
import com.twohands.auth_service.application.useraccount.viewloginsesssionlist.ViewLoginSesssionListUseCase;
import com.twohands.auth_service.common.dto.ApiResponse;
import com.twohands.auth_service.delivery.http.users.request.SoftDeleteAccountRequest;
import com.twohands.auth_service.delivery.http.users.request.TogglePrivacyRequest;
import com.twohands.auth_service.delivery.http.users.request.UpdateAvatarRequest;
import com.twohands.auth_service.delivery.http.users.request.UpdateProfileRequest;
import com.twohands.auth_service.delivery.http.users.request.UpdateUserSettingsRequest;
import com.twohands.auth_service.delivery.http.users.response.TrackLoginHistoryResponse;
import com.twohands.auth_service.delivery.http.users.response.UpdateUserSettingsResponse;
import com.twohands.auth_service.delivery.http.users.response.ViewAccountResponse;
import com.twohands.auth_service.delivery.http.users.response.ViewLoginSesssionListResponse;
import com.twohands.auth_service.exception.AppException;
import com.twohands.auth_service.exception.ErrorCode;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users/me")
public class UserAccountController {

    private final ViewAccountUseCase viewAccountUseCase;
    private final UpdateProfileUseCase updateProfileUseCase;
    private final UpdateAvatarUseCase updateAvatarUseCase;
    private final TogglePrivacyUseCase togglePrivacyUseCase;
    private final UpdateUserSettingsUseCase updateUserSettingsUseCase;
    private final SoftDeleteAccountUseCase softDeleteAccountUseCase;
    private final ViewLoginSesssionListUseCase viewLoginSesssionListUseCase;
    private final LogoutAllSesssionUseCase logoutAllSesssionUseCase;
    private final TrackLoginHistoryUseCase trackLoginHistoryUseCase;

    public UserAccountController(
            ViewAccountUseCase viewAccountUseCase,
            UpdateProfileUseCase updateProfileUseCase,
            UpdateAvatarUseCase updateAvatarUseCase,
            TogglePrivacyUseCase togglePrivacyUseCase,
            UpdateUserSettingsUseCase updateUserSettingsUseCase,
            SoftDeleteAccountUseCase softDeleteAccountUseCase,
            ViewLoginSesssionListUseCase viewLoginSesssionListUseCase,
            LogoutAllSesssionUseCase logoutAllSesssionUseCase,
            TrackLoginHistoryUseCase trackLoginHistoryUseCase
    ) {
        this.viewAccountUseCase = viewAccountUseCase;
        this.updateProfileUseCase = updateProfileUseCase;
        this.updateAvatarUseCase = updateAvatarUseCase;
        this.togglePrivacyUseCase = togglePrivacyUseCase;
        this.updateUserSettingsUseCase = updateUserSettingsUseCase;
        this.softDeleteAccountUseCase = softDeleteAccountUseCase;
        this.viewLoginSesssionListUseCase = viewLoginSesssionListUseCase;
        this.logoutAllSesssionUseCase = logoutAllSesssionUseCase;
        this.trackLoginHistoryUseCase = trackLoginHistoryUseCase;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<ViewAccountResponse>> getMe(Authentication authentication) {
        UUID userId = extractUserId(authentication);
        ViewAccountResult result = viewAccountUseCase.execute(userId);

        ViewAccountResponse response = new ViewAccountResponse(
                new ViewAccountResponse.UserData(
                        result.user().id().toString(),
                        result.user().email(),
                        result.user().status(),
                        result.user().emailVerified(),
                        result.user().phone(),
                        result.user().lastLoginAt()
                ),
                new ViewAccountResponse.ProfileData(
                        result.profile().displayName(),
                        result.profile().avatarUrl(),
                        result.profile().bio(),
                        result.profile().website(),
                        result.profile().socialLinks(),
                        result.profile().isPrivate()
                ),
                new ViewAccountResponse.SettingsData(result.settings().appearanceMode())
        );

        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success(HttpStatus.OK.value(), viewAccountUseCase.successMessage(), response));
    }

    @GetMapping("/sessions")
    public ResponseEntity<ApiResponse<ViewLoginSesssionListResponse>> getSessionList(Authentication authentication) {
        UUID userId = extractUserId(authentication);
        ViewLoginSesssionListResult result = viewLoginSesssionListUseCase.execute(userId);

        ViewLoginSesssionListResponse response = new ViewLoginSesssionListResponse(
                result.sessions().stream()
                        .map(session -> new ViewLoginSesssionListResponse.SessionData(
                                session.id().toString(),
                                session.deviceId(),
                                session.ipAddress(),
                                session.userAgent(),
                                session.status(),
                                session.createdAt(),
                                session.updatedAt(),
                                session.expiresAt()
                        ))
                        .toList()
        );

        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success(HttpStatus.OK.value(), viewLoginSesssionListUseCase.successMessage(), response));
    }

    @PostMapping("/sessions/logout-all")
    public ResponseEntity<ApiResponse<Void>> logoutAllSessions(Authentication authentication) {
        UUID userId = extractUserId(authentication);
        logoutAllSesssionUseCase.execute(userId);

        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success(HttpStatus.OK.value(), logoutAllSesssionUseCase.successMessage(), null));
    }

    @GetMapping("/login-history")
    public ResponseEntity<ApiResponse<TrackLoginHistoryResponse>> trackLoginHistory(
            Authentication authentication,
            @RequestParam(name = "limit", defaultValue = "20") int limit,
            @RequestParam(name = "offset", defaultValue = "0") int offset
    ) {
        validatePagination(limit, offset);

        UUID userId = extractUserId(authentication);
        TrackLoginHistoryResult result = trackLoginHistoryUseCase.execute(userId, limit, offset);

        TrackLoginHistoryResponse response = new TrackLoginHistoryResponse(
                result.items().stream()
                        .map(item -> new TrackLoginHistoryResponse.Item(
                                item.id().toString(),
                                item.loginMethod(),
                                item.ipAddress(),
                                item.userAgent(),
                                item.success(),
                                item.createdAt()
                        ))
                        .toList(),
                result.limit(),
                result.offset()
        );

        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success(HttpStatus.OK.value(), trackLoginHistoryUseCase.successMessage(), response));
    }

    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<Void>> updateProfile(
            @Valid @RequestBody UpdateProfileRequest request,
            Authentication authentication
    ) {
        UUID userId = extractUserId(authentication);
        updateProfileUseCase.execute(new UpdateProfileCommand(
                userId,
                request.displayName(),
                request.bio(),
                request.website(),
                request.socialLinks()
        ));

        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success(HttpStatus.OK.value(), updateProfileUseCase.successMessage(), null));
    }

    @PatchMapping("/avatar")
    public ResponseEntity<ApiResponse<Void>> updateAvatar(
            @Valid @RequestBody UpdateAvatarRequest request,
            Authentication authentication
    ) {
        UUID userId = extractUserId(authentication);
        updateAvatarUseCase.execute(new UpdateAvatarCommand(userId, request.avatarUrl()));

        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success(HttpStatus.OK.value(), updateAvatarUseCase.successMessage(), null));
    }

    @PatchMapping("/privacy")
    public ResponseEntity<ApiResponse<Void>> togglePrivacy(
            @Valid @RequestBody TogglePrivacyRequest request,
            Authentication authentication
    ) {
        UUID userId = extractUserId(authentication);
        togglePrivacyUseCase.execute(new TogglePrivacyCommand(userId, request.isPrivate()));

        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success(HttpStatus.OK.value(), togglePrivacyUseCase.successMessage(), null));
    }

    @PatchMapping("/settings")
    public ResponseEntity<ApiResponse<UpdateUserSettingsResponse>> updateSettings(
            @Valid @RequestBody UpdateUserSettingsRequest request,
            Authentication authentication
    ) {
        UUID userId = extractUserId(authentication);
        UpdateUserSettingsResult result = updateUserSettingsUseCase.execute(
                new UpdateUserSettingsCommand(userId, request.appearanceMode())
        );

        UpdateUserSettingsResponse response = new UpdateUserSettingsResponse(result.appearanceMode());

        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success(HttpStatus.OK.value(), updateUserSettingsUseCase.successMessage(), response));
    }

    @PostMapping("/soft-delete")
    public ResponseEntity<ApiResponse<Void>> softDelete(
            @Valid @RequestBody SoftDeleteAccountRequest request,
            Authentication authentication
    ) {
        UUID userId = extractUserId(authentication);
        softDeleteAccountUseCase.execute(new SoftDeleteAccountCommand(userId, request.password()));

        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success(HttpStatus.OK.value(), softDeleteAccountUseCase.successMessage(), null));
    }

    private UUID extractUserId(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new AppException(ErrorCode.UNAUTHORIZED, ErrorCode.UNAUTHORIZED.defaultMessage());
        }
        try {
            return UUID.fromString(authentication.getPrincipal().toString());
        } catch (IllegalArgumentException ex) {
            throw new AppException(ErrorCode.UNAUTHORIZED, ErrorCode.UNAUTHORIZED.defaultMessage());
        }
    }

    private void validatePagination(int limit, int offset) {
        if (limit < 1 || limit > 100) {
            throw new AppException(
                    ErrorCode.VALIDATION_ERROR,
                    "Limit khong hop le.",
                    "limit",
                    "INVALID_RANGE"
            );
        }
        if (offset < 0) {
            throw new AppException(
                    ErrorCode.VALIDATION_ERROR,
                    "Offset khong hop le.",
                    "offset",
                    "INVALID_RANGE"
            );
        }
    }
}
