package com.twohands.auth_service.application.auth.changepassword;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.twohands.auth_service.application.auth.register.PasswordHashingService;
import com.twohands.auth_service.domain.outbox.OutboxEvent;
import com.twohands.auth_service.domain.outbox.OutboxEventRepository;
import com.twohands.auth_service.domain.outbox.OutboxStatus;
import com.twohands.auth_service.domain.session.RefreshTokenSessionRepository;
import com.twohands.auth_service.domain.user.User;
import com.twohands.auth_service.domain.user.UserRepository;
import com.twohands.auth_service.exception.AppException;
import com.twohands.auth_service.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class ChangePasswordUseCase {

    private static final String CHANGE_PASSWORD_SUCCESS_MESSAGE = "Doi mat khau thanh cong.";
    private static final String PASSWORD_CHANGED_EVENT_TYPE = "PASSWORD_CHANGED";

    private final ChangePasswordValidationService validationService;
    private final UserRepository userRepository;
    private final PasswordHashingService passwordHashingService;
    private final RefreshTokenSessionRepository refreshTokenSessionRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    public ChangePasswordUseCase(
            ChangePasswordValidationService validationService,
            UserRepository userRepository,
            PasswordHashingService passwordHashingService,
            RefreshTokenSessionRepository refreshTokenSessionRepository,
            OutboxEventRepository outboxEventRepository,
            ObjectMapper objectMapper
    ) {
        this.validationService = validationService;
        this.userRepository = userRepository;
        this.passwordHashingService = passwordHashingService;
        this.refreshTokenSessionRepository = refreshTokenSessionRepository;
        this.outboxEventRepository = outboxEventRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public void execute(ChangePasswordCommand command) {
        if (command.userId() == null) {
            throw new AppException(ErrorCode.UNAUTHORIZED, ErrorCode.UNAUTHORIZED.defaultMessage());
        }

        validationService.validate(command);

        User user = userRepository.findById(command.userId())
                .orElseThrow(() -> new AppException(ErrorCode.UNAUTHORIZED, ErrorCode.UNAUTHORIZED.defaultMessage()));

        if (!passwordHashingService.matches(command.currentPassword(), user.passwordHash())) {
            throw new AppException(
                    ErrorCode.VALIDATION_ERROR,
                    "Mat khau hien tai khong chinh xac.",
                    "current_password",
                    "INVALID_CREDENTIAL"
            );
        }

        Instant now = Instant.now();
        var newPasswordHash = passwordHashingService.hash(command.newPassword());
        userRepository.updatePassword(user.id(), newPasswordHash, now);
        refreshTokenSessionRepository.revokeAllByUserId(user.id());
        outboxEventRepository.save(buildPasswordChangedOutboxEvent(user.id(), user.email().normalizedValue(), now));
    }

    public String successMessage() {
        return CHANGE_PASSWORD_SUCCESS_MESSAGE;
    }

    private OutboxEvent buildPasswordChangedOutboxEvent(UUID userId, String email, Instant now) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("user_id", userId.toString());
        payload.put("email", email);
        payload.put("changed_at", now.toString());

        String payloadJson;
        try {
            payloadJson = objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException ex) {
            throw new AppException(ErrorCode.INTERNAL_ERROR, "Cannot serialize outbox payload", ex);
        }

        return new OutboxEvent(
                UUID.randomUUID(),
                PASSWORD_CHANGED_EVENT_TYPE,
                "auth-service",
                payloadJson,
                OutboxStatus.PENDING,
                0,
                now,
                null,
                null
        );
    }
}
