package com.twohands.notification_service.domain.devicetoken;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserDeviceTokenRepository {

    UserDeviceToken save(UserDeviceToken token);

    Optional<UserDeviceToken> findByDeviceToken(String deviceToken);

    List<UserDeviceToken> findByUserIdOrderByActiveDescUpdatedAtDesc(UUID userId);

    boolean existsActiveByUserId(UUID userId);
}
