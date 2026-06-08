package com.twohands.social_service.application.post.common;

import com.twohands.social_service.exception.AppException;
import com.twohands.social_service.exception.ErrorCode;

public final class PostMediaDimensionValidator {

    private static final int MIN_DIMENSION = 1;
    private static final int MAX_DIMENSION = 10000;

    private PostMediaDimensionValidator() {
    }

    public static void validate(Integer width, Integer height, String fieldPrefix) {
        if (width == null && height == null) {
            return;
        }
        if (width == null || height == null) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "Validation failed",
                    fieldPrefix + ".width", "width va height phai duoc gui cung nhau.");
        }
        if (width < MIN_DIMENSION || width > MAX_DIMENSION) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "Validation failed",
                    fieldPrefix + ".width", "width phai tu " + MIN_DIMENSION + " den " + MAX_DIMENSION + ".");
        }
        if (height < MIN_DIMENSION || height > MAX_DIMENSION) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "Validation failed",
                    fieldPrefix + ".height", "height phai tu " + MIN_DIMENSION + " den " + MAX_DIMENSION + ".");
        }
    }
}
