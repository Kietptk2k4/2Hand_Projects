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

@Service
public class DeactivateInvalidDeviceTokenUseCase {

    private static final Logger log = LoggerFactory.getLogger(DeactivateInvalidDeviceTokenUseCase.class);

    private final UserDeviceTokenRepository userDeviceTokenRepository;

    public DeactivateInvalidDeviceTokenUseCase(UserDeviceTokenRepository userDeviceTokenRepository) {
        this.userDeviceTokenRepository = userDeviceTokenRepository;
    }

    @Transactional
    public DeactivateInvalidDeviceTokenResult execute(DeactivateInvalidDeviceTokenCommand command) {
        String normalizedToken;
        try {
            normalizedToken = RegisterDeviceTokenPolicy.normalizeDeviceToken(command.deviceToken());
        } catch (IllegalArgumentException ex) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "Validation failed", "deviceToken", ex.getMessage());
        }

        return userDeviceTokenRepository.findByDeviceToken(normalizedToken)
                .map(this::deactivateIfPresent)
                .orElseGet(() -> {
                    log.debug(
                            "Invalid device token cleanup skipped because token was not found token={}",
                            RegisterDeviceTokenPolicy.maskDeviceToken(normalizedToken)
                    );
                    return DeactivateInvalidDeviceTokenResult.notFound();
                });
    }

    private DeactivateInvalidDeviceTokenResult deactivateIfPresent(UserDeviceToken token) {
        RevokeDeviceTokenOutcome outcome = RevokeDeviceTokenPolicy.apply(token, Instant.now());
        if (!outcome.changed()) {
            log.debug(
                    "Invalid device token cleanup skipped because token is already inactive token={}",
                    RegisterDeviceTokenPolicy.maskDeviceToken(token.deviceToken())
            );
            return DeactivateInvalidDeviceTokenResult.alreadyInactive();
        }

        userDeviceTokenRepository.save(outcome.token());
        log.info(
                "Invalid device token deactivated userId={} token={}",
                token.userId(),
                RegisterDeviceTokenPolicy.maskDeviceToken(token.deviceToken())
        );
        return DeactivateInvalidDeviceTokenResult.deactivated();
    }
}
