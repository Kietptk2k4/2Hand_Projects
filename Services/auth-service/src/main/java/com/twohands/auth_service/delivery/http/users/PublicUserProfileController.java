package com.twohands.auth_service.delivery.http.users;

import com.twohands.auth_service.application.useraccount.viewpublicuserprofile.ViewPublicUserProfileResult;
import com.twohands.auth_service.application.useraccount.viewpublicuserprofile.ViewPublicUserProfileUseCase;
import com.twohands.auth_service.common.dto.ApiResponse;
import com.twohands.auth_service.delivery.http.users.response.ViewPublicUserProfileResponse;
import com.twohands.auth_service.exception.AppException;
import com.twohands.auth_service.exception.ErrorCode;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
public class PublicUserProfileController {

    private final ViewPublicUserProfileUseCase viewPublicUserProfileUseCase;

    public PublicUserProfileController(ViewPublicUserProfileUseCase viewPublicUserProfileUseCase) {
        this.viewPublicUserProfileUseCase = viewPublicUserProfileUseCase;
    }

    @GetMapping("/{userId}/public-profile")
    public ResponseEntity<ApiResponse<ViewPublicUserProfileResponse>> getPublicProfile(@PathVariable String userId) {
        UUID targetUserId = parseUserId(userId);
        ViewPublicUserProfileResult result = viewPublicUserProfileUseCase.execute(targetUserId);

        ViewPublicUserProfileResponse response = new ViewPublicUserProfileResponse(
                result.userId().toString(),
                result.displayName(),
                result.avatarUrl(),
                result.bio(),
                result.website(),
                result.socialLinks(),
                result.isPrivate()
        );

        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success(HttpStatus.OK.value(), viewPublicUserProfileUseCase.successMessage(), response));
    }

    private UUID parseUserId(String rawUserId) {
        try {
            return UUID.fromString(rawUserId);
        } catch (IllegalArgumentException ex) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "Du lieu khong hop le.", "userId", "INVALID_FORMAT");
        }
    }
}
