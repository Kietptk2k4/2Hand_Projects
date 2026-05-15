package com.twohands.auth_service.application.auth.changepassword;

import com.twohands.auth_service.exception.AppException;
import com.twohands.auth_service.exception.ErrorCode;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

@Service
public class ChangePasswordValidationService {

    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,32}$");

    public void validate(ChangePasswordCommand command) {
        validateCurrentPassword(command.currentPassword());
        validateNewPassword(command.newPassword());
        validateConfirmPassword(command.newPassword(), command.confirmNewPassword());
        validateNewPasswordDifferentFromCurrent(command.currentPassword(), command.newPassword());
    }

    private void validateCurrentPassword(String currentPassword) {
        if (currentPassword == null || currentPassword.isBlank()) {
            throw new AppException(
                    ErrorCode.VALIDATION_ERROR,
                    "Vui long nhap mat khau hien tai.",
                    "current_password",
                    "REQUIRED"
            );
        }
    }

    private void validateNewPassword(String newPassword) {
        if (newPassword == null || newPassword.isBlank()) {
            throw new AppException(
                    ErrorCode.VALIDATION_ERROR,
                    "Mat khau moi la bat buoc.",
                    "new_password",
                    "REQUIRED"
            );
        }
        if (!PASSWORD_PATTERN.matcher(newPassword).matches()) {
            throw new AppException(
                    ErrorCode.VALIDATION_ERROR,
                    "Mat khau moi khong dat yeu cau do manh.",
                    "new_password",
                    "WEAK"
            );
        }
    }

    private void validateConfirmPassword(String newPassword, String confirmNewPassword) {
        if (confirmNewPassword == null || confirmNewPassword.isBlank()) {
            throw new AppException(
                    ErrorCode.VALIDATION_ERROR,
                    "Xac nhan mat khau moi la bat buoc.",
                    "confirm_new_password",
                    "REQUIRED"
            );
        }
        if (!confirmNewPassword.equals(newPassword)) {
            throw new AppException(
                    ErrorCode.VALIDATION_ERROR,
                    "Xac nhan mat khau moi khong khop.",
                    "confirm_new_password",
                    "MISMATCH"
            );
        }
    }

    private void validateNewPasswordDifferentFromCurrent(String currentPassword, String newPassword) {
        if (currentPassword != null && currentPassword.equals(newPassword)) {
            throw new AppException(
                    ErrorCode.VALIDATION_ERROR,
                    "Mat khau moi phai khac mat khau hien tai.",
                    "new_password",
                    "SAME_AS_CURRENT"
            );
        }
    }
}
