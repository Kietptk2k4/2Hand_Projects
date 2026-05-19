package com.twohands.social_service.domain.follow;

import com.twohands.social_service.exception.AppException;
import com.twohands.social_service.exception.ErrorCode;

public enum RelationListType {
    FOLLOWERS,
    FOLLOWING;

    public static RelationListType fromQuery(String type) {
        if (type == null || type.isBlank()) {
            throw new AppException(ErrorCode.BAD_REQUEST, "Tham so type bat buoc.");
        }
        return switch (type.trim().toLowerCase()) {
            case "followers" -> FOLLOWERS;
            case "following" -> FOLLOWING;
            default -> throw new AppException(ErrorCode.BAD_REQUEST, "Type chi chap nhan followers hoac following.");
        };
    }

    public String queryValue() {
        return name().toLowerCase();
    }
}
