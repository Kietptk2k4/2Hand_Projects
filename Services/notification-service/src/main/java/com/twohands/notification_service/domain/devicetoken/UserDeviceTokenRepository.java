package com.twohands.notification_service.domain.devicetoken;

import java.util.Optional;

public interface UserDeviceTokenRepository {

    UserDeviceToken save(UserDeviceToken token);

    Optional<UserDeviceToken> findByDeviceToken(String deviceToken);
}
