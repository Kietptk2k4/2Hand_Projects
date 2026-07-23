package com.twohands.social_service.application.comment.common;

import com.twohands.social_service.exception.AppException;
import com.twohands.social_service.exception.ErrorCode;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Component;

@Component
public class CommentIdValidator {

    public void validate(String commentId) {
        if (commentId == null || commentId.isBlank() || !ObjectId.isValid(commentId)) {
            throw new AppException(ErrorCode.BAD_REQUEST, "commentId khong hop le.");
        }
    }
}
