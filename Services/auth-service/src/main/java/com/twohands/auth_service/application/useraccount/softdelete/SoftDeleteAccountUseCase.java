package com.twohands.auth_service.application.useraccount.softdelete;

import com.twohands.auth_service.application.auth.register.PasswordHashingService;
import com.twohands.auth_service.application.useraccount.common.UserAccountAuthContextService;
import com.twohands.auth_service.application.useraccount.common.UserAccountOutboxService;
import com.twohands.auth_service.domain.outbox.OutboxEventRepository;
import com.twohands.auth_service.domain.session.RefreshTokenSessionRepository;
import com.twohands.auth_service.domain.user.User;
import com.twohands.auth_service.domain.user.UserRepository;
import com.twohands.auth_service.domain.user.UserStatus;
import com.twohands.auth_service.exception.AppException;
import com.twohands.auth_service.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class SoftDeleteAccountUseCase {

    private static final String SUCCESS_MESSAGE = "Xoa tai khoan thanh cong.";

    private final SoftDeleteAccountValidationService validationService;
    private final UserRepository userRepository;
    private final RefreshTokenSessionRepository refreshTokenSessionRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final PasswordHashingService passwordHashingService;
    private final UserAccountOutboxService outboxService;
    private final UserAccountAuthContextService authContextService;

    public SoftDeleteAccountUseCase(
            SoftDeleteAccountValidationService validationService,
            UserRepository userRepository,
            RefreshTokenSessionRepository refreshTokenSessionRepository,
            OutboxEventRepository outboxEventRepository,
            PasswordHashingService passwordHashingService,
            UserAccountOutboxService outboxService,
            UserAccountAuthContextService authContextService
    ) {
        this.validationService = validationService;
        this.userRepository = userRepository;
        this.refreshTokenSessionRepository = refreshTokenSessionRepository;
        this.outboxEventRepository = outboxEventRepository;
        this.passwordHashingService = passwordHashingService;
        this.outboxService = outboxService;
        this.authContextService = authContextService;
    }

    @Transactional
    public void execute(SoftDeleteAccountCommand command) {
        UUID userId = authContextService.requireUserId(command.userId());
        validationService.validate(command);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.UNAUTHORIZED, ErrorCode.UNAUTHORIZED.defaultMessage()));

        if (user.status() == UserStatus.DELETED) {
            throw new AppException(ErrorCode.CONFLICT, "Tai khoan da bi xoa.");
        }

        if (!passwordHashingService.matches(command.password(), user.passwordHash())) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "Mat khau khong chinh xac.", "password", "INVALID_CREDENTIAL");
        }

        Instant now = Instant.now();
        user.softDelete(now);

        userRepository.updateStatusDeleted(user.id(), now);
        refreshTokenSessionRepository.revokeAllByUserId(user.id());
        outboxEventRepository.save(outboxService.userDeleted(user.id(), user.email().normalizedValue(), now));
    }

    public String successMessage() {
        return SUCCESS_MESSAGE;
    }
}
