package com.twohands.auth_service.application.useraccount.trackloginhistory;

import com.twohands.auth_service.application.useraccount.common.UserAccountAuthContextService;
import com.twohands.auth_service.domain.user.LoginLogRepository;
import com.twohands.auth_service.domain.user.User;
import com.twohands.auth_service.domain.user.UserRepository;
import com.twohands.auth_service.exception.AppException;
import com.twohands.auth_service.exception.ErrorCode;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class TrackLoginHistoryUseCase {

    private static final String SUCCESS_MESSAGE = "Lay lich su dang nhap thanh cong.";

    private final UserRepository userRepository;
    private final LoginLogRepository loginLogRepository;
    private final UserAccountAuthContextService authContextService;

    public TrackLoginHistoryUseCase(
            UserRepository userRepository,
            LoginLogRepository loginLogRepository,
            UserAccountAuthContextService authContextService
    ) {
        this.userRepository = userRepository;
        this.loginLogRepository = loginLogRepository;
        this.authContextService = authContextService;
    }

    public TrackLoginHistoryResult execute(UUID actorUserId, int limit, int offset) {
        UUID userId = authContextService.requireUserId(actorUserId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.UNAUTHORIZED, ErrorCode.UNAUTHORIZED.defaultMessage()));
        authContextService.ensureUserActive(user.status());

        List<TrackLoginHistoryResult.Item> items = loginLogRepository.findByUserId(userId, limit, offset)
                .stream()
                .map(log -> new TrackLoginHistoryResult.Item(
                        log.id(),
                        log.loginMethod().name(),
                        log.ipAddress(),
                        log.userAgent(),
                        log.success(),
                        log.createdAt()
                ))
                .toList();

        return new TrackLoginHistoryResult(items, limit, offset);
    }

    public String successMessage() {
        return SUCCESS_MESSAGE;
    }
}
