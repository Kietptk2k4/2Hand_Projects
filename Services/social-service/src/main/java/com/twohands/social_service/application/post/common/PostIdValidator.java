package com.twohands.social_service.application.post.common;

import com.twohands.social_service.exception.AppException;
import com.twohands.social_service.exception.ErrorCode;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Component;

@Component
public class PostIdValidator {

    public void validate(String postId) {
        if (postId == null || postId.isBlank() || !ObjectId.isValid(postId)) {
            throw new AppException(ErrorCode.BAD_REQUEST, "postId khong hop le.");
        }
    }
}
