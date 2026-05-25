package com.twohands.notification_service.application.devicetoken;

import com.twohands.notification_service.domain.devicetoken.UserDeviceTokenRepository;
import com.twohands.notification_service.domain.devicetoken.ViewUserDeviceTokensPolicy;
import com.twohands.notification_service.exception.AppException;
import com.twohands.notification_service.exception.ErrorCode;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class ViewUserDeviceTokensUseCase {

    private final UserDeviceTokenRepository userDeviceTokenRepository;

    public ViewUserDeviceTokensUseCase(UserDeviceTokenRepository userDeviceTokenRepository) {
        this.userDeviceTokenRepository = userDeviceTokenRepository;
    }

    public ViewUserDeviceTokensResult execute(ViewUserDeviceTokensCommand command) {
        validateCommand(command);

        return new ViewUserDeviceTokensResult(
                command.userId(),
                ViewUserDeviceTokensPolicy.toViews(
                        userDeviceTokenRepository.findByUserIdOrderByActiveDescUpdatedAtDesc(command.userId())
                )
        );
    }

    public String successMessage() {
        return "Device tokens retrieved successfully";
    }

    private void validateCommand(ViewUserDeviceTokensCommand command) {
        if (command.userId() == null) {
            throw new AppException(ErrorCode.UNAUTHORIZED, "Authentication required");
        }
    }
}
