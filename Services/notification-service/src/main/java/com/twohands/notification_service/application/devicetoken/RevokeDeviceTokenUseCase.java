package com.twohands.notification_service.application.devicetoken;

import com.twohands.notification_service.domain.devicetoken.RegisterDeviceTokenPolicy;
import com.twohands.notification_service.domain.devicetoken.RevokeDeviceTokenOutcome;
import com.twohands.notification_service.domain.devicetoken.RevokeDeviceTokenPolicy;
import com.twohands.notification_service.domain.devicetoken.UserDeviceToken;
import com.twohands.notification_service.domain.devicetoken.UserDeviceTokenRepository;
import com.twohands.notification_service.exception.AppException;
import com.twohands.notification_service.exception.ErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class RevokeDeviceTokenUseCase {

    private static final Logger log = LoggerFactory.getLogger(RevokeDeviceTokenUseCase.class);

    private final UserDeviceTokenRepository userDeviceTokenRepository;

    public RevokeDeviceTokenUseCase(UserDeviceTokenRepository userDeviceTokenRepository) {
        this.userDeviceTokenRepository = userDeviceTokenRepository;
    }

    @Transactional
    public RevokeDeviceTokenResult execute(RevokeDeviceTokenCommand command) {
        validateCommand(command);

        String normalizedToken;
        try {
            normalizedToken = RegisterDeviceTokenPolicy.normalizeDeviceToken(command.deviceToken());
        } catch (IllegalArgumentException ex) {
            throw validationError("deviceToken", ex.getMessage());
        }

        UserDeviceToken token = userDeviceTokenRepository.findByDeviceToken(normalizedToken)
                .filter(found -> found.userId().equals(command.userId()))
                .orElseThrow(() -> new AppException(
                        ErrorCode.DEVICE_TOKEN_NOT_FOUND,
                        "Device token not found"
                ));

        RevokeDeviceTokenOutcome outcome = RevokeDeviceTokenPolicy.apply(token, Instant.now());
        UserDeviceToken saved = outcome.changed()
                ? userDeviceTokenRepository.save(outcome.token())
                : outcome.token();

        log.info(
                "Device token revoked userId={} token={} alreadyRevoked={}",
                command.userId(),
                RegisterDeviceTokenPolicy.maskDeviceToken(normalizedToken),
                !outcome.changed()
        );

        return new RevokeDeviceTokenResult(
                saved.id(),
                saved.active(),
                !outcome.changed()
        );
    }

    public String successMessage() {
        return "Device token revoked successfully";
    }

    private void validateCommand(RevokeDeviceTokenCommand command) {
        if (command.userId() == null) {
            throw new AppException(ErrorCode.UNAUTHORIZED, "Authentication required");
        }
    }

    private AppException validationError(String field, String reason) {
        return new AppException(ErrorCode.VALIDATION_ERROR, "Validation failed", field, reason);
    }
}
