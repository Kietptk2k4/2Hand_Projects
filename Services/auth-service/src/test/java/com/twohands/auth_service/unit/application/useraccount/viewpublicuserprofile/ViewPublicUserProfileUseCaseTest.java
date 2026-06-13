package com.twohands.auth_service.unit.application.useraccount.viewpublicuserprofile;

import com.twohands.auth_service.application.useraccount.viewpublicuserprofile.ViewPublicUserProfileResult;
import com.twohands.auth_service.application.useraccount.viewpublicuserprofile.ViewPublicUserProfileUseCase;
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
import org.mockito.Mockito;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

class ViewPublicUserProfileUseCaseTest {

    private final UserRepository userRepository = Mockito.mock(UserRepository.class);
    private final UserProfileRepository userProfileRepository = Mockito.mock(UserProfileRepository.class);

    private ViewPublicUserProfileUseCase useCase;
    private UUID targetUserId;

    @BeforeEach
    void setup() {
        useCase = new ViewPublicUserProfileUseCase(userRepository, userProfileRepository);
        targetUserId = UUID.randomUUID();
    }

    @Test
    void shouldReturnFullFieldsWhenPublicProfile() {
        when(userRepository.findById(targetUserId)).thenReturn(Optional.of(buildUser(UserStatus.ACTIVE)));
        when(userProfileRepository.findByUserId(targetUserId)).thenReturn(Optional.of(buildProfile(false)));

        ViewPublicUserProfileResult result = useCase.execute(targetUserId);

        assertEquals("Public User", result.displayName());
        assertEquals("https://example.com", result.website());
        assertEquals("https://github.com/public-user", result.socialLinks().get("github"));
        assertEquals(false, result.isPrivate());
    }

    @Test
    void shouldMaskSensitiveFieldsWhenPrivateProfile() {
        when(userRepository.findById(targetUserId)).thenReturn(Optional.of(buildUser(UserStatus.ACTIVE)));
        when(userProfileRepository.findByUserId(targetUserId)).thenReturn(Optional.of(buildProfile(true)));

        ViewPublicUserProfileResult result = useCase.execute(targetUserId);

        assertEquals("Public User", result.displayName());
        assertEquals(null, result.bio());
        assertEquals(null, result.website());
        assertEquals(0, result.socialLinks().size());
        assertEquals(true, result.isPrivate());
    }

    @Test
    void shouldThrow404WhenUserNotFound() {
        when(userRepository.findById(targetUserId)).thenReturn(Optional.empty());

        AppException ex = assertThrows(AppException.class, () -> useCase.execute(targetUserId));
        assertEquals(ErrorCode.RESOURCE_NOT_FOUND, ex.getErrorCode());
    }

    @Test
    void shouldThrow404WhenUserDeleted() {
        when(userRepository.findById(targetUserId)).thenReturn(Optional.of(buildUser(UserStatus.DELETED)));

        AppException ex = assertThrows(AppException.class, () -> useCase.execute(targetUserId));
        assertEquals(ErrorCode.RESOURCE_NOT_FOUND, ex.getErrorCode());
    }

    private User buildUser(UserStatus status) {
        Instant now = Instant.now();
        return User.rehydrate(
                targetUserId,
                EmailAddress.of("public-profile@example.com"),
                PasswordHash.of("$2a$10$abcdefghijklmnopqrstuv"),
                status,
                true,
                false,
                null,
                null,
                status == UserStatus.DELETED ? now : null,
                now.minusSeconds(1000),
                now.minusSeconds(1000)
        );
    }

    private UserProfile buildProfile(boolean isPrivate) {
        Instant now = Instant.now();
        return UserProfile.rehydrate(
                targetUserId,
                "Public User",
                "https://minio.local/2hands-avatar/public-user.png",
                null,
                "Bio text",
                "https://example.com",
                Map.of("github", "https://github.com/public-user"),
                isPrivate,
                now.minusSeconds(1000),
                now.minusSeconds(1000)
        );
    }
}
