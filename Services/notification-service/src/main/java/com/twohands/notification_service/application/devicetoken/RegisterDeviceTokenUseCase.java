package com.twohands.notification_service.application.devicetoken;

import com.twohands.notification_service.domain.devicetoken.DeviceType;
import com.twohands.notification_service.domain.devicetoken.RegisterDeviceTokenDecision;
import com.twohands.notification_service.domain.devicetoken.RegisterDeviceTokenPolicy;
import com.twohands.notification_service.domain.devicetoken.UserDeviceToken;
import com.twohands.notification_service.domain.devicetoken.UserDeviceTokenRepository;
import com.twohands.notification_service.exception.AppException;
import com.twohands.notification_service.exception.ErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class RegisterDeviceTokenUseCase {

    private static final Logger log = LoggerFactory.getLogger(RegisterDeviceTokenUseCase.class);

    private final UserDeviceTokenRepository userDeviceTokenRepository;

    public RegisterDeviceTokenUseCase(UserDeviceTokenRepository userDeviceTokenRepository) {
        this.userDeviceTokenRepository = userDeviceTokenRepository;
    }

    @Transactional
    public RegisterDeviceTokenResult execute(RegisterDeviceTokenCommand command) {
        validateCommand(command);

        UUID userId = command.userId();
        DeviceType deviceType;
        String normalizedToken;
        try {
            deviceType = RegisterDeviceTokenPolicy.parseDeviceType(command.deviceType());
            normalizedToken = RegisterDeviceTokenPolicy.normalizeDeviceToken(command.deviceToken());
        } catch (IllegalArgumentException ex) {
            throw mapValidationException(ex);
        }

        Optional<UserDeviceToken> existing = userDeviceTokenRepository.findByDeviceToken(normalizedToken);
        RegisterDeviceTokenDecision decision = RegisterDeviceTokenPolicy.resolve(
                deviceType,
                normalizedToken,
                existing,
                userId
        );

        Instant now = Instant.now();
        UUID tokenId = existing.map(UserDeviceToken::id).orElse(UUID.randomUUID());
        Instant createdAt = existing.map(UserDeviceToken::createdAt).orElse(now);

        UserDeviceToken saved = userDeviceTokenRepository.save(
                new UserDeviceToken(
                        tokenId,
                        userId,
                        decision.deviceType(),
                        decision.deviceToken(),
                        true,
                        now,
                        now,
                        createdAt
                )
        );

        log.info(
                "Device token registered userId={} deviceType={} token={} alreadyRegistered={}",
                userId,
                saved.deviceType(),
                RegisterDeviceTokenPolicy.maskDeviceToken(saved.deviceToken()),
                decision.alreadyRegistered()
        );

        return new RegisterDeviceTokenResult(
                saved.id(),
                saved.userId(),
                saved.deviceType(),
                saved.active(),
                saved.createdAt(),
                saved.updatedAt(),
                saved.lastUsedAt(),
                decision.alreadyRegistered()
        );
    }

    public String successMessage() {
        return "Device token registered successfully";
    }

    private void validateCommand(RegisterDeviceTokenCommand command) {
        if (command.userId() == null) {
            throw new AppException(ErrorCode.UNAUTHORIZED, "Authentication required");
        }
    }

    private AppException mapValidationException(IllegalArgumentException ex) {
        if ("Invalid device type.".equals(ex.getMessage())) {
            return validationError("deviceType", "Device type must be one of IOS, ANDROID, WEB.");
        }
        return validationError("deviceToken", ex.getMessage());
    }

    private AppException validationError(String field, String reason) {
        return new AppException(ErrorCode.VALIDATION_ERROR, "Validation failed", field, reason);
    }
}
