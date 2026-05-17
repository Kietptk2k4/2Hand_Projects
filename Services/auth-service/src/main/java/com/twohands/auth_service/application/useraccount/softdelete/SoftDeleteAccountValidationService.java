package com.twohands.auth_service.application.useraccount.softdelete;

import com.twohands.auth_service.exception.AppException;
import com.twohands.auth_service.exception.ErrorCode;
import org.springframework.stereotype.Service;

@Service
public class SoftDeleteAccountValidationService {

    public void validate(SoftDeleteAccountCommand command) {
        if (command.password() == null || command.password().isBlank()) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "Password is required", "password", "REQUIRED");
        }
    }
}
