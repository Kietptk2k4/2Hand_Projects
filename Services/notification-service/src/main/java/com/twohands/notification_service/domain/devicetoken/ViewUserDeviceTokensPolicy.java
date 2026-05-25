package com.twohands.notification_service.domain.devicetoken;

import java.util.List;

public final class ViewUserDeviceTokensPolicy {

    private ViewUserDeviceTokensPolicy() {
    }

    public static List<UserDeviceTokenView> toViews(List<UserDeviceToken> tokens) {
        return tokens.stream()
                .map(ViewUserDeviceTokensPolicy::toView)
                .toList();
    }

    public static UserDeviceTokenView toView(UserDeviceToken token) {
        return new UserDeviceTokenView(
                token.id(),
                token.deviceType(),
                RegisterDeviceTokenPolicy.maskDeviceToken(token.deviceToken()),
                token.active(),
                token.lastUsedAt(),
                token.createdAt(),
                token.updatedAt()
        );
    }
}
