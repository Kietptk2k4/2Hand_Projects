package com.twohands.auth_service.application.useraccount.viewloginsesssionlist;

import com.twohands.auth_service.application.useraccount.common.UserAccountAuthContextService;
import com.twohands.auth_service.domain.session.RefreshTokenSession;
import com.twohands.auth_service.domain.session.RefreshTokenSessionRepository;
import com.twohands.auth_service.domain.session.SessionStatus;
import com.twohands.auth_service.domain.user.User;
import com.twohands.auth_service.domain.user.UserRepository;
import com.twohands.auth_service.exception.AppException;
import com.twohands.auth_service.exception.ErrorCode;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ViewLoginSesssionListUseCase {

    private static final String SUCCESS_MESSAGE = "Lay danh sach phien dang nhap thanh cong.";

    private final UserRepository userRepository;
    private final RefreshTokenSessionRepository refreshTokenSessionRepository;
    private final UserAccountAuthContextService authContextService;

    public ViewLoginSesssionListUseCase(
            UserRepository userRepository,
            RefreshTokenSessionRepository refreshTokenSessionRepository,
            UserAccountAuthContextService authContextService
    ) {
        this.userRepository = userRepository;
        this.refreshTokenSessionRepository = refreshTokenSessionRepository;
        this.authContextService = authContextService;
    }

    public ViewLoginSesssionListResult execute(UUID actorUserId) {
        UUID userId = authContextService.requireUserId(actorUserId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.UNAUTHORIZED, ErrorCode.UNAUTHORIZED.defaultMessage()));
        authContextService.ensureUserActive(user.status());

        List<ViewLoginSesssionListResult.SessionData> sessions = refreshTokenSessionRepository
                .findByUserIdAndStatus(userId, SessionStatus.ACTIVE)
                .stream()
                .map(this::mapSession)
                .toList();

        return new ViewLoginSesssionListResult(sessions);
    }

    public String successMessage() {
        return SUCCESS_MESSAGE;
    }

    private ViewLoginSesssionListResult.SessionData mapSession(RefreshTokenSession session) {
        return new ViewLoginSesssionListResult.SessionData(
                session.id(),
                session.deviceId(),
                session.ipAddress(),
                session.userAgent(),
                session.status().name(),
                session.createdAt(),
                session.updatedAt(),
                session.expiresAt()
        );
    }
}
