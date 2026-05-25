package com.twohands.notification_service.delivery.http.devicetoken.mapper;

import com.twohands.notification_service.application.devicetoken.ViewUserDeviceTokensResult;
import com.twohands.notification_service.delivery.http.devicetoken.response.DeviceTokenItemResponse;
import com.twohands.notification_service.delivery.http.devicetoken.response.ViewUserDeviceTokensResponse;
import com.twohands.notification_service.domain.devicetoken.UserDeviceTokenView;
import org.springframework.stereotype.Component;

@Component
public class ViewUserDeviceTokensHttpMapper {

    public ViewUserDeviceTokensResponse toResponse(ViewUserDeviceTokensResult result) {
        return new ViewUserDeviceTokensResponse(
                result.items().stream()
                        .map(this::toItemResponse)
                        .toList()
        );
    }

    private DeviceTokenItemResponse toItemResponse(UserDeviceTokenView item) {
        return new DeviceTokenItemResponse(
                item.id(),
                item.deviceType(),
                item.maskedDeviceToken(),
                item.active(),
                item.lastUsedAt(),
                item.createdAt(),
                item.updatedAt()
        );
    }
}
