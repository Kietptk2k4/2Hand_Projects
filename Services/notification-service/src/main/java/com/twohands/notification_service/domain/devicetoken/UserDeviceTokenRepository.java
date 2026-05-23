package com.twohands.notification_service.domain.devicetoken;

import java.util.Optional;
import java.util.UUID;

public interface UserDeviceTokenRepository {

    UserDeviceToken save(UserDeviceToken token);

    Optional<UserDeviceToken> findByDeviceToken(String deviceToken);

    boolean existsActiveByUserId(UUID userId);
}
