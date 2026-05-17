package com.twohands.auth_service.application.useraccount.viewpublicuserprofile;

import com.twohands.auth_service.domain.user.User;
import com.twohands.auth_service.domain.user.UserProfile;
import com.twohands.auth_service.domain.user.UserProfileRepository;
import com.twohands.auth_service.domain.user.UserRepository;
import com.twohands.auth_service.domain.user.UserStatus;
import com.twohands.auth_service.exception.AppException;
import com.twohands.auth_service.exception.ErrorCode;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Service
public class ViewPublicUserProfileUseCase {

    private static final String SUCCESS_MESSAGE = "Lay public profile thanh cong.";

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;

    public ViewPublicUserProfileUseCase(
            UserRepository userRepository,
            UserProfileRepository userProfileRepository
    ) {
        this.userRepository = userRepository;
        this.userProfileRepository = userProfileRepository;
    }

    public ViewPublicUserProfileResult execute(UUID targetUserId) {
        User user = userRepository.findById(targetUserId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, ErrorCode.RESOURCE_NOT_FOUND.defaultMessage()));

        if (user.status() == UserStatus.DELETED) {
            throw new AppException(ErrorCode.RESOURCE_NOT_FOUND, ErrorCode.RESOURCE_NOT_FOUND.defaultMessage());
        }

        UserProfile profile = userProfileRepository.findByUserId(targetUserId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, ErrorCode.RESOURCE_NOT_FOUND.defaultMessage()));

        if (profile.isPrivate()) {
            return new ViewPublicUserProfileResult(
                    targetUserId,
                    profile.displayName(),
                    profile.avatarUrl(),
                    null,
                    null,
                    Map.of(),
                    true
            );
        }

        return new ViewPublicUserProfileResult(
                targetUserId,
                profile.displayName(),
                profile.avatarUrl(),
                profile.bio(),
                profile.website(),
                profile.socialLinks(),
                false
        );
    }

    public String successMessage() {
        return SUCCESS_MESSAGE;
    }
}
